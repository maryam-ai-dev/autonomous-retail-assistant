package com.aisleon.basket.application;

import com.aisleon.basket.BasketItem;
import com.aisleon.basket.SubstitutionFlag;
import com.aisleon.basket.infrastructure.BasketItemJpaEntity;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.catalogue.DietaryTag;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Converts between domain BasketItem and the JPA entity. */
public final class BasketMapper {

    private BasketMapper() {}

    public static BasketItemJpaEntity toJpa(BasketItem item, String reasoning) {
        NormalizedProduct p = item.product();
        BasketItemJpaEntity entity = BasketItemJpaEntity.builder()
                .externalProductId(p.externalId())
                .retailer(p.retailer())
                .canonicalName(p.canonicalName() == null ? p.displayName() : p.canonicalName())
                .displayName(p.displayName())
                .brand(p.brand())
                .price(p.price())
                .quantity(item.quantity())
                .imageUrl(p.imageUrl())
                .productUrl(p.productUrl())
                .reasoning(reasoning)
                .dietaryTags(p.dietaryTags().stream().map(Enum::name).toList())
                .build();
        item.substitutionFlag().ifPresent(flag -> {
            entity.setSubstitutionFlagType(flag.reason());
            entity.setSubstitutionFlagReason(flag.message());
            entity.setSubstitutionFlagResolved(flag.resolved());
        });
        return entity;
    }

    public static BasketItem fromJpa(BasketItemJpaEntity entity) {
        NormalizedProduct p = new NormalizedProduct(
                entity.getExternalProductId(),
                entity.getCanonicalName(),
                entity.getDisplayName(),
                entity.getBrand(),
                entity.getRetailer(),
                ProductCategory.UNKNOWN,
                ProductSubcategory.UNKNOWN,
                entity.getPrice(),
                null,
                null,
                null,
                entity.getImageUrl(),
                entity.getProductUrl(),
                true,
                true,
                parseDietary(entity.getDietaryTags()),
                List.of(),
                List.of(),
                1.0,
                Instant.now(),
                List.of(),
                List.of());
        Optional<SubstitutionFlag> flag = Optional.empty();
        if (entity.getSubstitutionFlagType() != null) {
            flag = Optional.of(new SubstitutionFlag(
                    entity.getSubstitutionFlagType(),
                    entity.getSubstitutionFlagReason() == null
                            ? ""
                            : entity.getSubstitutionFlagReason(),
                    entity.getSubstitutionFlagResolved()));
        }
        String itemId = entity.getId() == null
                ? UUID.randomUUID().toString()
                : entity.getId().toString();
        return new BasketItem(itemId, p, entity.getQuantity(), flag);
    }

    public static BigDecimal total(BasketJpaEntity basket) {
        BigDecimal total = BigDecimal.ZERO;
        for (BasketItemJpaEntity item : basket.getItems()) {
            total = total.add(item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private static List<DietaryTag> parseDietary(List<String> raw) {
        if (raw == null) return List.of();
        List<DietaryTag> out = new ArrayList<>();
        for (String s : raw) {
            try {
                out.add(DietaryTag.valueOf(s));
            } catch (IllegalArgumentException ignored) {
                // unknown dietary tag persisted — ignore on re-read
            }
        }
        return out;
    }
}
