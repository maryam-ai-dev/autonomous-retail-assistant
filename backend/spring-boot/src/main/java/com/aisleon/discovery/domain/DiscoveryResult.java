package com.aisleon.discovery.domain;

import com.aisleon.discovery.application.RankedDiscoveryResult;

import java.time.Instant;
import java.util.List;

/**
 * Structured internal response wrapping normalised products from discovery,
 * enriched with ranking, confidence, and uncertainty data from the Python service.
 */
public record DiscoveryResult(
        List<NormalizedProduct> products,
        List<String> sourcesUsed,
        int totalFound,
        Instant queryProcessedAt,
        double confidence,
        String strategyUsed,
        RankedDiscoveryResult.UncertaintyDto uncertainty,
        List<RankedDiscoveryResult.RankedProductDto> rankedProducts
) {
}
