package com.aisleon.catalogue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RawScraperProduct(
        String externalId,
        String displayName,
        String brand,
        ProductCategory category,
        ProductSubcategory subcategory,
        BigDecimal price,
        boolean priceFromText,
        BigDecimal unitPrice,
        String unitBasis,
        String sizeText,
        String imageUrl,
        String productUrl,
        boolean isAvailable,
        boolean isBasketable,
        List<CertificationTag> certificationTags,
        List<OfferFlag> offerFlags,
        Instant sourceFetchedAt) {
    public RawScraperProduct {
        if (certificationTags == null) certificationTags = List.of();
        if (offerFlags == null) offerFlags = List.of();
    }
}
