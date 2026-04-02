package com.aisleon.discovery.domain;

import com.aisleon.merchant.domain.SourceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A product normalised from any connector source into a common structure.
 * merchantId is nullable but merchantName must always be set.
 */
public record NormalizedProduct(
        SourceType sourceType,
        String sourceName,
        String externalProductId,
        String title,
        String description,
        String category,
        String brand,
        BigDecimal price,
        String currency,
        String availability,
        UUID merchantId,
        String merchantName,
        Double merchantRating,
        BigDecimal shippingCost,
        String shippingEta,
        List<String> imageUrls,
        String productUrl,
        Map<String, String> attributes,
        Instant lastSyncedAt
) {
}
