package com.aisleon.checkout.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Checkout order aggregate root. Pure Java, no JPA or Spring imports.
 * Status transitions: INITIATED → PROCESSING → COMPLETED | FAILED
 */
public class CheckoutOrder {

    private final UUID id;
    private final UUID userId;
    private final UUID cartId;
    private final UUID approvalId;
    private CheckoutOrderStatus status;
    private String executorType;
    private String merchantOrderRef;
    private BigDecimal totalAmount;
    private String currency;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;

    public CheckoutOrder(UUID id,
                         UUID userId,
                         UUID cartId,
                         UUID approvalId,
                         CheckoutOrderStatus status,
                         String executorType,
                         String merchantOrderRef,
                         BigDecimal totalAmount,
                         String currency,
                         LocalDateTime createdAt,
                         LocalDateTime completedAt,
                         String errorMessage) {
        this.id = id;
        this.userId = userId;
        this.cartId = cartId;
        this.approvalId = approvalId;
        this.status = status;
        this.executorType = executorType;
        this.merchantOrderRef = merchantOrderRef;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
    }

    /**
     * Transition to PROCESSING. Must be INITIATED.
     */
    public void startProcessing() {
        assertStatus(CheckoutOrderStatus.INITIATED, "start processing");
        this.status = CheckoutOrderStatus.PROCESSING;
    }

    /**
     * Transition to COMPLETED. Must be PROCESSING.
     */
    public void completeOrder(String merchantOrderRef) {
        assertStatus(CheckoutOrderStatus.PROCESSING, "complete");
        this.status = CheckoutOrderStatus.COMPLETED;
        this.merchantOrderRef = merchantOrderRef;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Transition to FAILED. Must be PROCESSING.
     */
    public void failOrder(String reason) {
        assertStatus(CheckoutOrderStatus.PROCESSING, "fail");
        this.status = CheckoutOrderStatus.FAILED;
        this.errorMessage = reason;
        this.completedAt = LocalDateTime.now();
    }

    private void assertStatus(CheckoutOrderStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + " checkout order — current status is " + this.status
                            + ", expected " + expected);
        }
    }

    // Getters

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCartId() { return cartId; }
    public UUID getApprovalId() { return approvalId; }
    public CheckoutOrderStatus getStatus() { return status; }
    public String getExecutorType() { return executorType; }
    public String getMerchantOrderRef() { return merchantOrderRef; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }

    public void setExecutorType(String executorType) { this.executorType = executorType; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setCurrency(String currency) { this.currency = currency; }
}
