package com.aisleon.cart.interfaces;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {

    @NotBlank(message = "External product ID is required")
    private String externalProductId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String currency;

    private UUID merchantId;

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    private Double merchantRating;
    private String sourceType;
    private String sourceName;
    private String productUrl;
    private String imageUrl;
}
