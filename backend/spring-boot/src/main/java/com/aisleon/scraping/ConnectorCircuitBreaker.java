package com.aisleon.scraping;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Failure-window circuit breaker:
 *   - CLOSED   → normal operation
 *   - OPEN     → opened after {@link #failureThreshold} failures in
 *                {@link #failureWindow}; stays OPEN for {@link #openDuration}
 *   - HALF_OPEN → first call after openDuration elapses; a success returns to
 *                CLOSED, a failure returns to OPEN (resetting the timer)
 */
public class ConnectorCircuitBreaker {

    public static final Duration DEFAULT_FAILURE_WINDOW = Duration.ofSeconds(60);
    public static final Duration DEFAULT_OPEN_DURATION = Duration.ofSeconds(120);
    public static final int DEFAULT_FAILURE_THRESHOLD = 3;

    private final Clock clock;
    private final int failureThreshold;
    private final Duration failureWindow;
    private final Duration openDuration;

    private final Deque<Instant> failures = new ArrayDeque<>();
    private final AtomicReference<CircuitState> state =
            new AtomicReference<>(CircuitState.CLOSED);
    private final AtomicReference<Instant> openedAt = new AtomicReference<>();

    public ConnectorCircuitBreaker() {
        this(Clock.systemUTC(), DEFAULT_FAILURE_THRESHOLD, DEFAULT_FAILURE_WINDOW,
                DEFAULT_OPEN_DURATION);
    }

    public ConnectorCircuitBreaker(
            Clock clock,
            int failureThreshold,
            Duration failureWindow,
            Duration openDuration) {
        this.clock = clock;
        this.failureThreshold = failureThreshold;
        this.failureWindow = failureWindow;
        this.openDuration = openDuration;
    }

    public synchronized CircuitState currentState() {
        maybeTransitionToHalfOpen();
        return state.get();
    }

    public synchronized boolean allowRequest() {
        maybeTransitionToHalfOpen();
        return state.get() != CircuitState.OPEN;
    }

    public synchronized void recordSuccess() {
        failures.clear();
        state.set(CircuitState.CLOSED);
        openedAt.set(null);
    }

    public synchronized void recordFailure() {
        Instant now = Instant.now(clock);
        failures.addLast(now);
        pruneOldFailures(now);
        if (state.get() == CircuitState.HALF_OPEN) {
            openCircuit(now);
            return;
        }
        if (failures.size() >= failureThreshold) {
            openCircuit(now);
        }
    }

    private void openCircuit(Instant now) {
        state.set(CircuitState.OPEN);
        openedAt.set(now);
    }

    private void maybeTransitionToHalfOpen() {
        if (state.get() != CircuitState.OPEN) return;
        Instant openedAtValue = openedAt.get();
        if (openedAtValue == null) return;
        if (Duration.between(openedAtValue, Instant.now(clock)).compareTo(openDuration) >= 0) {
            state.set(CircuitState.HALF_OPEN);
        }
    }

    private void pruneOldFailures(Instant now) {
        while (!failures.isEmpty()
                && Duration.between(failures.peekFirst(), now).compareTo(failureWindow) > 0) {
            failures.pollFirst();
        }
    }
}
