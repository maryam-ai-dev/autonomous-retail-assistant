package com.aisleon.cart.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cart item entity within the Cart aggregate. Pure Java, no JPA annotations.
 */
public class CartItem {

    private final UUID id;
    private final String externalProductId;
    private final String title;
    private final BigDecimal price;
    private final String currency;
    private final UUID merchantId;
    private final String merchantName;
    private final Double merchantRating;
    private final String sourceType;
    private final String sourceName;
    private final String productUrl;
    private final String imageUrl;
    private final boolean isSubstitution;
    private final String originalProductId;
    private final LocalDateTime addedAt;

    public CartItem(UUID id,
                    String externalProductId,
                    String title,
                    BigDecimal price,
                    String currency,
                    UUID merchantId,
                    String merchantName,
                    Double merchantRating,
                    String sourceType,
                    String sourceName,
                    String productUrl,
                    String imageUrl,
                    boolean isSubstitution,
                    String originalProductId,
                    LocalDateTime addedAt) {
        this.id = id;
        this.externalProductId = externalProductId;
        this.title = title;
        this.price = price;
        this.currency = currency;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.merchantRating = merchantRating;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.isSubstitution = isSubstitution;
        this.originalProductId = originalProductId;
        this.addedAt = addedAt;
    }

    public UUID getId() { return id; }
    public String getExternalProductId() { return externalProductId; }
    public String getTitle() { return title; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public UUID getMerchantId() { return merchantId; }
    public String getMerchantName() { return merchantName; }
    public Double getMerchantRating() { return merchantRating; }
    public String getSourceType() { return sourceType; }
    public String getSourceName() { return sourceName; }
    public String getProductUrl() { return productUrl; }
    public String getImageUrl() { return imageUrl; }
    public boolean isSubstitution() { return isSubstitution; }
    public String getOriginalProductId() { return originalProductId; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
