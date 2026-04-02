package com.aisleon.approval.infrastructure;

import com.aisleon.approval.domain.ApprovalRequest;
import com.aisleon.approval.domain.ApprovalStatus;

/**
 * Maps between ApprovalRequest domain object and JPA entity.
 * This is the only place where domain - JPA translation happens for approvals.
 */
public class ApprovalRequestMapper {

    private ApprovalRequestMapper() {
    }

    public static ApprovalRequest toDomain(ApprovalRequestJpaEntity entity) {
        return new ApprovalRequest(
                entity.getId(),
                entity.getUserId(),
                entity.getCartId(),
                ApprovalStatus.valueOf(entity.getStatus()),
                entity.getTriggerReason(),
                entity.getTotalAmount(),
                entity.getRequiresUserAction() != null && entity.getRequiresUserAction(),
                entity.getCreatedAt(),
                entity.getDecidedAt(),
                entity.getDecision()
        );
    }

    public static ApprovalRequestJpaEntity toEntity(ApprovalRequest domain) {
        return ApprovalRequestJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .cartId(domain.getCartId())
                .status(domain.getStatus().name())
                .triggerReason(domain.getTriggerReason())
                .totalAmount(domain.getTotalAmount())
                .requiresUserAction(domain.isRequiresUserAction())
                .createdAt(domain.getCreatedAt())
                .decidedAt(domain.getDecidedAt())
                .decision(domain.getDecision())
                .build();
    }
}
