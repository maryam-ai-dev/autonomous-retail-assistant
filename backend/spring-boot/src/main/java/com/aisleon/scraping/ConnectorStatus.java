package com.aisleon.scraping;

import com.aisleon.catalogue.Retailer;
import java.time.Instant;

public record ConnectorStatus(
        Retailer retailer,
        boolean healthy,
        boolean disabled,
        CircuitState circuitState,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        String lastFailureReason,
        int recentResultCount,
        int staleCacheUsageCount,
        boolean apifyConnector) {}
