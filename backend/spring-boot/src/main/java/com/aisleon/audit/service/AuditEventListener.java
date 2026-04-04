package com.aisleon.audit.service;

import com.aisleon.approval.events.ApprovalRequestedEvent;
import com.aisleon.approval.events.PurchaseAuthorizedEvent;
import com.aisleon.approval.events.PurchaseRejectedEvent;
import com.aisleon.audit.repository.AuditEventJpaEntity;
import com.aisleon.audit.repository.AuditRepository;
import com.aisleon.cart.events.ApprovalRequiredEvent;
import com.aisleon.cart.events.CartCheckedOutEvent;
import com.aisleon.checkout.events.CheckoutCompletedEvent;
import com.aisleon.checkout.events.CheckoutFailedEvent;
import com.aisleon.common.events.ProductCandidatesRankedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuditEventListener(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @EventListener
    @Async("auditExecutor")
    public void onApprovalRequested(ApprovalRequestedEvent event) {
        saveEvent(event.userId(), "APPROVAL_REQUESTED", "approval", event.approvalId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onPurchaseAuthorized(PurchaseAuthorizedEvent event) {
        saveEvent(event.userId(), "PURCHASE_AUTHORIZED", "approval", event.approvalId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onPurchaseRejected(PurchaseRejectedEvent event) {
        saveEvent(event.userId(), "PURCHASE_REJECTED", "approval", event.approvalId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onCartCheckedOut(CartCheckedOutEvent event) {
        saveEvent(event.userId(), "CART_CHECKOUT_INITIATED", "cart", event.cartId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onApprovalRequired(ApprovalRequiredEvent event) {
        saveEvent(event.userId(), "APPROVAL_REQUIRED", "cart", event.cartId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onCheckoutCompleted(CheckoutCompletedEvent event) {
        saveEvent(event.userId(), "CHECKOUT_COMPLETED", "checkout", event.orderId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onCheckoutFailed(CheckoutFailedEvent event) {
        saveEvent(event.userId(), "CHECKOUT_FAILED", "checkout", event.cartId().toString(), event);
    }

    @EventListener
    @Async("auditExecutor")
    public void onProductCandidatesRanked(ProductCandidatesRankedEvent event) {
        saveEvent(event.userId(), "PRODUCTS_RANKED", "discovery", null, event);
    }

    @SuppressWarnings("unchecked")
    private void saveEvent(java.util.UUID userId, String eventType,
                           String entityType, String entityId, Object event) {
        try {
            Map<String, Object> payload = objectMapper.convertValue(event, Map.class);

            AuditEventJpaEntity entity = AuditEventJpaEntity.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .payload(payload)
                    .build();

            auditRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to persist audit event {}: {}", eventType, e.getMessage());
        }
    }
}
