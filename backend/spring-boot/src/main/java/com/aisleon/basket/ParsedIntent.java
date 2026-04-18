package com.aisleon.basket;

import com.aisleon.catalogue.ProductCategory;
import java.math.BigDecimal;
import java.util.List;

public record ParsedIntent(
        String rawText,
        BigDecimal budget,
        ProductCategory primaryCategory,
        List<String> tags,
        boolean halalRequired,
        List<String> normalizationWarnings) {
    public ParsedIntent {
        if (tags == null) tags = List.of();
        if (normalizationWarnings == null) normalizationWarnings = List.of();
    }
}
