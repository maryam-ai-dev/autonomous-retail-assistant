package com.aisleon.catalogue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record NormalizedProduct(
        String externalId,
        String canonicalName,
        String displayName,
        String brand,
        Retailer retailer,
        ProductCategory category,
        ProductSubcategory subcategory,
        BigDecimal price,
        BigDecimal unitPrice,
        String unitBasis,
        String sizeText,
        String imageUrl,
        String productUrl,
        boolean isAvailable,
        boolean isBasketable,
        List<DietaryTag> dietaryTags,
        List<CertificationTag> certificationTags,
        List<OfferFlag> offerFlags,
        double confidenceScore,
        Instant sourceFetchedAt,
        List<String> normalizationWarnings,
        List<String> crossRetailerProductIds
) {
    public NormalizedProduct {
        if (dietaryTags == null) dietaryTags = List.of();
        if (certificationTags == null) certificationTags = List.of();
        if (offerFlags == null) offerFlags = List.of();
        if (normalizationWarnings == null) normalizationWarnings = List.of();
        if (crossRetailerProductIds == null) crossRetailerProductIds = List.of();
    }
}
