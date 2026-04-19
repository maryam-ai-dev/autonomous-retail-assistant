package com.aisleon.scraping;

import com.aisleon.catalogue.Retailer;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ValidationFailureRecorder {

    public record Entry(Instant at, String reason) {}

    private final Map<Retailer, Entry> latest = new EnumMap<>(Retailer.class);

    public synchronized void record(Retailer retailer, String reason) {
        latest.put(retailer, new Entry(Instant.now(), reason));
    }

    public synchronized Optional<Entry> latestFor(Retailer retailer) {
        return Optional.ofNullable(latest.get(retailer));
    }
}
