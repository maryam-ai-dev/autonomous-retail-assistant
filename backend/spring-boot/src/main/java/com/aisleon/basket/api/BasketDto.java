package com.aisleon.basket.api;

import com.aisleon.basket.BasketStatus;
import com.aisleon.basket.infrastructure.BasketItemJpaEntity;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A persisted basket — either DRAFT, APPROVED, or CHECKED_OUT")
public class BasketDto {

    private UUID id;
    private UUID basketIntentId;
    private BasketStatus status;
    private BigDecimal totalCost;
    private String currency;
    private List<String> retailersUsed;
    private boolean trimmed;
    private int trimmedItemCount;
    private List<BasketItemDto> items;
    private LocalDateTime createdAt;

    public static BasketDto fromEntity(BasketJpaEntity basket) {
        return BasketDto.builder()
                .id(basket.getId())
                .basketIntentId(basket.getBasketIntentId())
                .status(basket.getStatus())
                .totalCost(basket.getTotalCost())
                .currency(basket.getCurrency())
                .retailersUsed(basket.getRetailersUsed())
                .trimmed(Boolean.TRUE.equals(basket.getTrimmed()))
                .trimmedItemCount(basket.getTrimmedItemCount() == null ? 0 : basket.getTrimmedItemCount())
                .items(basket.getItems().stream()
                        .map(BasketDto::toItemDto)
                        .collect(Collectors.toList()))
                .createdAt(basket.getCreatedAt())
                .build();
    }

    private static BasketItemDto toItemDto(BasketItemJpaEntity item) {
        return BasketItemDto.builder()
                .id(item.getId())
                .externalProductId(item.getExternalProductId())
                .retailer(item.getRetailer().name())
                .canonicalName(item.getCanonicalName())
                .displayName(item.getDisplayName())
                .brand(item.getBrand())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .imageUrl(item.getImageUrl())
                .productUrl(item.getProductUrl())
                .reasoning(item.getReasoning())
                .dietaryTags(item.getDietaryTags())
                .substitutionFlagType(item.getSubstitutionFlagType())
                .substitutionFlagReason(item.getSubstitutionFlagReason())
                .substitutionFlagResolved(Boolean.TRUE.equals(item.getSubstitutionFlagResolved()))
                .build();
    }
}
