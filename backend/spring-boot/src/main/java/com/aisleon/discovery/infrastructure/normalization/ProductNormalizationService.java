package com.aisleon.discovery.infrastructure.normalization;

import com.aisleon.discovery.domain.NormalizedProduct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cleans and normalises products from any connector.
 * This service is source-agnostic — it does not know which connector produced the products.
 */
@Service
public class ProductNormalizationService {

    private static final int MAX_TITLE_LENGTH = 500;

    public List<NormalizedProduct> normalize(List<NormalizedProduct> products) {
        return products.stream()
                .map(this::clean)
                .filter(p -> p.price() != null && p.price().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    private NormalizedProduct clean(NormalizedProduct p) {
        return new NormalizedProduct(
                p.sourceType(),
                trimOrEmpty(p.sourceName()),
                trimOrEmpty(p.externalProductId()),
                truncate(trimOrEmpty(p.title()), MAX_TITLE_LENGTH),
                trimOrEmpty(p.description()),
                trimOrEmpty(p.category()),
                defaultIfBlank(trim(p.brand()), "Unknown"),
                p.price(),
                p.currency() != null ? p.currency().trim().toUpperCase() : "USD",
                trimOrEmpty(p.availability()),
                p.merchantId(),
                trimOrEmpty(p.merchantName()),
                p.merchantRating(),
                p.shippingCost() != null ? p.shippingCost() : BigDecimal.ZERO,
                trimOrEmpty(p.shippingEta()),
                p.imageUrls() != null ? p.imageUrls() : List.of(),
                trimOrEmpty(p.productUrl()),
                p.attributes() != null ? trimMapValues(p.attributes()) : Map.of(),
                p.lastSyncedAt()
        );
    }

    private String trimOrEmpty(String value) {
        return value != null ? value.trim() : "";
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private Map<String, String> trimMapValues(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().trim(),
                        e -> e.getValue() != null ? e.getValue().trim() : ""
                ));
    }
}
