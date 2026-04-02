package com.aisleon.discovery.domain;

import java.time.Instant;
import java.util.List;

/**
 * Structured internal response wrapping normalised products from discovery.
 */
public record DiscoveryResult(
        List<NormalizedProduct> products,
        List<String> sourcesUsed,
        int totalFound,
        Instant queryProcessedAt
) {
}
