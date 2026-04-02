package com.aisleon.approval.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Approval request aggregate root. Pure Java, no JPA or Spring imports.
 */
public class ApprovalRequest {

    private final UUID id;
    private final UUID userId;
    private final UUID cartId;
    private ApprovalStatus status;
    private final String triggerReason;
    private final BigDecimal totalAmount;
    private final boolean requiresUserAction;
    private final LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private String decision;

    public ApprovalRequest(UUID id,
                           UUID userId,
                           UUID cartId,
                           ApprovalStatus status,
                           String triggerReason,
                           BigDecimal totalAmount,
                           boolean requiresUserAction,
                           LocalDateTime createdAt,
                           LocalDateTime decidedAt,
                           String decision) {
        this.id = id;
        this.userId = userId;
        this.cartId = cartId;
        this.status = status;
        this.triggerReason = triggerReason;
        this.totalAmount = totalAmount;
        this.requiresUserAction = requiresUserAction;
        this.createdAt = createdAt;
        this.decidedAt = decidedAt;
        this.decision = decision;
    }

    /**
     * Transition to APPROVED. Throws if not currently PENDING.
     */
    public void approve() {
        assertPending("approve");
        this.status = ApprovalStatus.APPROVED;
        this.decision = "APPROVED";
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * Transition to REJECTED. Throws if not currently PENDING.
     */
    public void reject() {
        assertPending("reject");
        this.status = ApprovalStatus.REJECTED;
        this.decision = "REJECTED";
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * Transition to EXPIRED.
     */
    public void expire() {
        this.status = ApprovalStatus.EXPIRED;
        this.decision = "EXPIRED";
        this.decidedAt = LocalDateTime.now();
    }

    private void assertPending(String action) {
        if (this.status != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot " + action + " approval request — current status is " + this.status);
        }
    }

    // Getters

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCartId() { return cartId; }
    public ApprovalStatus getStatus() { return status; }
    public String getTriggerReason() { return triggerReason; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public boolean isRequiresUserAction() { return requiresUserAction; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public String getDecision() { return decision; }
}
