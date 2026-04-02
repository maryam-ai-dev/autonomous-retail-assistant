package com.aisleon.cart.infrastructure;

import com.aisleon.cart.domain.Cart;
import com.aisleon.cart.domain.CartItem;
import com.aisleon.cart.domain.CartStatus;

import java.util.List;

/**
 * Maps between Cart domain objects and JPA entities.
 * This is the only place where domain ↔ JPA translation happens for cart.
 */
public class CartMapper {

    private CartMapper() {
    }

    public static Cart toDomain(CartJpaEntity entity) {
        List<CartItem> items = entity.getItems().stream()
                .map(CartMapper::itemToDomain)
                .toList();

        return new Cart(
                entity.getId(),
                entity.getUserId(),
                CartStatus.valueOf(entity.getStatus()),
                items
        );
    }

    public static CartJpaEntity toEntity(Cart domain) {
        CartJpaEntity cartEntity = CartJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .status(domain.getStatus().name())
                .build();

        List<CartItemJpaEntity> itemEntities = domain.getItems().stream()
                .map(item -> itemToEntity(item, cartEntity))
                .toList();

        cartEntity.setItems(new java.util.ArrayList<>(itemEntities));
        return cartEntity;
    }

    private static CartItem itemToDomain(CartItemJpaEntity entity) {
        return new CartItem(
                entity.getId(),
                entity.getExternalProductId(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getMerchantId(),
                entity.getMerchantName(),
                entity.getMerchantRating(),
                entity.getSourceType(),
                entity.getSourceName(),
                entity.getProductUrl(),
                entity.getImageUrl(),
                entity.getIsSubstitution() != null && entity.getIsSubstitution(),
                entity.getOriginalProductId(),
                entity.getAddedAt()
        );
    }

    private static CartItemJpaEntity itemToEntity(CartItem item, CartJpaEntity cart) {
        return CartItemJpaEntity.builder()
                .id(item.getId())
                .cart(cart)
                .externalProductId(item.getExternalProductId())
                .title(item.getTitle())
                .price(item.getPrice())
                .currency(item.getCurrency())
                .merchantId(item.getMerchantId())
                .merchantName(item.getMerchantName())
                .merchantRating(item.getMerchantRating())
                .sourceType(item.getSourceType())
                .sourceName(item.getSourceName())
                .productUrl(item.getProductUrl())
                .imageUrl(item.getImageUrl())
                .isSubstitution(item.isSubstitution())
                .originalProductId(item.getOriginalProductId())
                .addedAt(item.getAddedAt())
                .build();
    }
}
