package com.aisleon.checkout.infrastructure;

import com.aisleon.checkout.domain.CheckoutOrder;
import com.aisleon.checkout.domain.CheckoutOrderStatus;

/**
 * Maps between CheckoutOrder domain object and JPA entity.
 * This is the only place where domain ↔ JPA translation happens for checkout orders.
 */
public class CheckoutOrderMapper {

    private CheckoutOrderMapper() {
    }

    public static CheckoutOrder toDomain(CheckoutOrderJpaEntity entity) {
        return new CheckoutOrder(
                entity.getId(),
                entity.getUserId(),
                entity.getCartId(),
                entity.getApprovalId(),
                CheckoutOrderStatus.valueOf(entity.getStatus()),
                entity.getExecutorType(),
                entity.getMerchantOrderRef(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                entity.getErrorMessage()
        );
    }

    public static CheckoutOrderJpaEntity toEntity(CheckoutOrder domain) {
        return CheckoutOrderJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .cartId(domain.getCartId())
                .approvalId(domain.getApprovalId())
                .status(domain.getStatus().name())
                .executorType(domain.getExecutorType())
                .merchantOrderRef(domain.getMerchantOrderRef())
                .totalAmount(domain.getTotalAmount())
                .currency(domain.getCurrency())
                .createdAt(domain.getCreatedAt())
                .completedAt(domain.getCompletedAt())
                .errorMessage(domain.getErrorMessage())
                .build();
    }
}
