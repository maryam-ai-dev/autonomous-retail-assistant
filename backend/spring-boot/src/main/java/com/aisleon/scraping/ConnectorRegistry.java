package com.aisleon.scraping;

import com.aisleon.catalogue.Retailer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConnectorRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConnectorRegistry.class);

    private final Map<Retailer, RetailerConnector> connectors;
    private final Set<Retailer> disabled;
    private final ConcurrentMap<Retailer, ConnectorStatus> latestStatus = new ConcurrentHashMap<>();
    private final StaleCacheCounter staleCounter;
    private final ValidationFailureRecorder validationFailureRecorder;

    public ConnectorRegistry(
            List<RetailerConnector> registeredConnectors,
            @Value("${connector.disabled:${CONNECTOR_DISABLED:}}") String disabledCsv,
            StaleCacheCounter staleCounter,
            ValidationFailureRecorder validationFailureRecorder) {
        Map<Retailer, RetailerConnector> map = new HashMap<>();
        for (RetailerConnector c : registeredConnectors) {
            map.put(c.getRetailer(), c);
        }
        this.connectors = Collections.unmodifiableMap(map);
        this.disabled = parseDisabled(disabledCsv);
        this.staleCounter = staleCounter;
        this.validationFailureRecorder = validationFailureRecorder;
        refreshStatuses();
    }

    public RetailerConnector getConnector(Retailer retailer) {
        if (disabled.contains(retailer)) {
            throw new ConnectorUnavailableException(retailer, "disabled by configuration");
        }
        RetailerConnector connector = connectors.get(retailer);
        if (connector == null) {
            throw new ConnectorUnavailableException(retailer, "no connector registered");
        }
        if (!connector.isHealthy()) {
            throw new ConnectorUnavailableException(retailer, "connector reports unhealthy");
        }
        return connector;
    }

    public Set<Retailer> availableRetailers() {
        return connectors.entrySet().stream()
                .filter(entry -> !disabled.contains(entry.getKey()))
                .filter(entry -> entry.getValue().isHealthy())
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    public List<ConnectorStatus> allStatuses() {
        List<ConnectorStatus> out = connectors.entrySet().stream()
                .map(entry -> buildStatus(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        out.forEach(status -> latestStatus.put(status.retailer(), status));
        return out;
    }

    public Optional<ConnectorStatus> statusFor(Retailer retailer) {
        return Optional.ofNullable(latestStatus.get(retailer));
    }

    public boolean isDisabled(Retailer retailer) {
        return disabled.contains(retailer);
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void refreshStatuses() {
        try {
            allStatuses();
        } catch (RuntimeException e) {
            log.warn("Connector status refresh failed: {}", e.getMessage());
        }
    }

    private ConnectorStatus buildStatus(Retailer retailer, RetailerConnector connector) {
        ConnectorStatus raw = connector.getStatus();
        boolean disabledForRetailer = disabled.contains(retailer);
        Instant failureAt = raw.lastFailureAt();
        String failureReason = raw.lastFailureReason();
        Optional<ValidationFailureRecorder.Entry> validation =
                validationFailureRecorder.latestFor(retailer);
        if (validation.isPresent()) {
            Instant validationAt = validation.get().at();
            if (failureAt == null || validationAt.isAfter(failureAt)) {
                failureAt = validationAt;
                failureReason = validation.get().reason();
            }
        }
        return new ConnectorStatus(
                retailer,
                raw.healthy() && !disabledForRetailer,
                disabledForRetailer,
                raw.circuitState(),
                raw.lastSuccessAt(),
                failureAt,
                failureReason,
                raw.recentResultCount(),
                staleCounter.countLastHour(retailer),
                raw.apifyConnector());
    }

    private static Set<Retailer> parseDisabled(String csv) {
        if (csv == null || csv.isBlank()) return EnumSet.noneOf(Retailer.class);
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Retailer.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown retailer in CONNECTOR_DISABLED: {}", s);
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Retailer.class)));
    }
}
