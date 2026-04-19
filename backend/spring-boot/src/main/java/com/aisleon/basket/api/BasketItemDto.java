package com.aisleon.basket.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single product line in a basket")
public class BasketItemDto {

    private UUID id;
    private String externalProductId;
    private String retailer;
    private String canonicalName;
    private String displayName;
    private String brand;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private String productUrl;
    private String reasoning;
    private List<String> dietaryTags;
    private String substitutionFlagType;
    private String substitutionFlagReason;
    private boolean substitutionFlagResolved;
}
