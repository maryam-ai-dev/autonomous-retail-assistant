package com.aisleon.scraping;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ConnectorCircuitBreakerTest {

    @Test
    void threeFailuresWithinWindowOpensCircuit() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-04-01T10:00:00Z"));
        ConnectorCircuitBreaker breaker = new ConnectorCircuitBreaker(
                Clock.fixed(now.get(), ZoneOffset.UTC),
                3,
                Duration.ofSeconds(60),
                Duration.ofSeconds(120));
        // 3 failures back-to-back (same clock instant — they're within the 60s window)
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.currentState()).isEqualTo(CircuitState.OPEN);
        assertThat(breaker.allowRequest()).isFalse();
    }

    @Test
    void transitionsToHalfOpenAfterOpenDuration() {
        Instant start = Instant.parse("2026-04-01T10:00:00Z");
        ManualClock clock = new ManualClock(start);
        ConnectorCircuitBreaker breaker = new ConnectorCircuitBreaker(
                clock, 3, Duration.ofSeconds(60), Duration.ofSeconds(120));
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.currentState()).isEqualTo(CircuitState.OPEN);

        clock.advance(Duration.ofSeconds(121));

        assertThat(breaker.currentState()).isEqualTo(CircuitState.HALF_OPEN);
    }

    @Test
    void successInHalfOpenReturnsToClosed() {
        ManualClock clock = new ManualClock(Instant.parse("2026-04-01T10:00:00Z"));
        ConnectorCircuitBreaker breaker = new ConnectorCircuitBreaker(
                clock, 3, Duration.ofSeconds(60), Duration.ofSeconds(120));
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        clock.advance(Duration.ofSeconds(121));
        // observe transition to half-open, then success closes
        assertThat(breaker.currentState()).isEqualTo(CircuitState.HALF_OPEN);
        breaker.recordSuccess();
        assertThat(breaker.currentState()).isEqualTo(CircuitState.CLOSED);
    }

    @Test
    void failuresOutsideWindowDoNotOpen() {
        ManualClock clock = new ManualClock(Instant.parse("2026-04-01T10:00:00Z"));
        ConnectorCircuitBreaker breaker = new ConnectorCircuitBreaker(
                clock, 3, Duration.ofSeconds(60), Duration.ofSeconds(120));
        breaker.recordFailure();
        clock.advance(Duration.ofSeconds(70));
        breaker.recordFailure();
        clock.advance(Duration.ofSeconds(70));
        breaker.recordFailure();
        assertThat(breaker.currentState()).isEqualTo(CircuitState.CLOSED);
    }

    private static final class ManualClock extends Clock {
        private Instant now;

        ManualClock(Instant start) {
            this.now = start;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
