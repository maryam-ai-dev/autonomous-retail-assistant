package com.aisleon.scraping;

import com.aisleon.catalogue.Retailer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Rolling 1-hour counter of stale-cache serves per retailer. */
@Component
public class StaleCacheCounter {

    private static final Duration WINDOW = Duration.ofHours(1);

    private final Map<Retailer, Deque<Instant>> log = new EnumMap<>(Retailer.class);

    public synchronized void record(Retailer retailer) {
        log.computeIfAbsent(retailer, r -> new ArrayDeque<>()).addLast(Instant.now());
    }

    public synchronized int countLastHour(Retailer retailer) {
        Deque<Instant> queue = log.get(retailer);
        if (queue == null) return 0;
        Instant cutoff = Instant.now().minus(WINDOW);
        while (!queue.isEmpty() && queue.peekFirst().isBefore(cutoff)) {
            queue.pollFirst();
        }
        return queue.size();
    }
}
