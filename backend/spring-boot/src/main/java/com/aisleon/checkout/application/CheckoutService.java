package com.aisleon.checkout.application;

import com.aisleon.approval.application.ApprovalService;
import com.aisleon.approval.domain.ApprovalRequest;
import com.aisleon.approval.domain.ApprovalStatus;
import com.aisleon.approval.events.PurchaseAuthorizedEvent;
import com.aisleon.cart.domain.Cart;
import com.aisleon.cart.infrastructure.CartJpaEntity;
import com.aisleon.cart.infrastructure.CartMapper;
import com.aisleon.cart.infrastructure.CartRepository;
import com.aisleon.checkout.domain.CheckoutOrder;
import com.aisleon.checkout.domain.CheckoutOrderStatus;
import com.aisleon.checkout.events.CheckoutCompletedEvent;
import com.aisleon.checkout.events.CheckoutFailedEvent;
import com.aisleon.checkout.infrastructure.CheckoutOrderJpaEntity;
import com.aisleon.checkout.infrastructure.CheckoutOrderMapper;
import com.aisleon.checkout.infrastructure.CheckoutOrderRepository;
import com.aisleon.checkout.infrastructure.executors.CheckoutExecutor;
import com.aisleon.checkout.infrastructure.executors.CheckoutResult;
import com.aisleon.checkout.infrastructure.executors.api.MockMarketplaceCheckoutExecutor;
import com.aisleon.checkout.infrastructure.executors.browser.BrowserCheckoutExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CheckoutOrderRepository checkoutOrderRepository;
    private final CartRepository cartRepository;
    private final ApprovalService approvalService;
    private final ApplicationEventPublisher eventPublisher;
    private final MockMarketplaceCheckoutExecutor mockExecutor;
    private final BrowserCheckoutExecutor browserExecutor;

    public CheckoutService(CheckoutOrderRepository checkoutOrderRepository,
                           CartRepository cartRepository,
                           ApprovalService approvalService,
                           ApplicationEventPublisher eventPublisher,
                           MockMarketplaceCheckoutExecutor mockExecutor,
                           BrowserCheckoutExecutor browserExecutor) {
        this.checkoutOrderRepository = checkoutOrderRepository;
        this.cartRepository = cartRepository;
        this.approvalService = approvalService;
        this.eventPublisher = eventPublisher;
        this.mockExecutor = mockExecutor;
        this.browserExecutor = browserExecutor;
    }

    /**
     * Executes checkout for an approved purchase. Tries API mock first, falls back to browser stub.
     */
    @Transactional
    public CheckoutOrder executeCheckout(UUID approvalId, UUID userId) {
        ApprovalRequest approval = approvalService.getApprovalById(approvalId);

        if (approval.getStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException(
                    "Cannot execute checkout — approval " + approvalId + " status is " + approval.getStatus());
        }

        CartJpaEntity cartEntity = cartRepository.findById(approval.getCartId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cart not found: " + approval.getCartId()));
        Cart cart = CartMapper.toDomain(cartEntity);

        // Create checkout order in INITIATED state
        CheckoutOrder order = new CheckoutOrder(
                UUID.randomUUID(),
                userId,
                cart.getId(),
                approvalId,
                CheckoutOrderStatus.INITIATED,
                null,
                null,
                cart.getTotalAmount(),
                "USD",
                LocalDateTime.now(),
                null,
                null
        );

        order.startProcessing();

        // Try API mock first, fall back to browser stub
        CheckoutExecutor executor = mockExecutor;
        CheckoutResult result = executor.execute(cart, userId);

        if (!result.success()) {
            log.info("API mock executor failed, trying browser executor");
            executor = browserExecutor;
            result = executor.execute(cart, userId);
        }

        order.setExecutorType(result.executorType());

        if (result.success()) {
            order.completeOrder(result.merchantOrderRef());

            // Update cart status to CHECKED_OUT
            cartEntity.setStatus("CHECKED_OUT");
            cartEntity.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cartEntity);

            checkoutOrderRepository.save(CheckoutOrderMapper.toEntity(order));

            eventPublisher.publishEvent(new CheckoutCompletedEvent(
                    userId, order.getId(), result.merchantOrderRef(), cart.getTotalAmount()
            ));

            log.info("Checkout completed — order {} for user {}", order.getId(), userId);
        } else {
            order.failOrder(result.errorMessage());
            checkoutOrderRepository.save(CheckoutOrderMapper.toEntity(order));

            eventPublisher.publishEvent(new CheckoutFailedEvent(
                    userId, cart.getId(), result.errorMessage()
            ));

            log.warn("Checkout failed — cart {} for user {}: {}", cart.getId(), userId, result.errorMessage());
        }

        return order;
    }

    @EventListener
    public void onPurchaseAuthorized(PurchaseAuthorizedEvent event) {
        log.info("Purchase authorized — triggering checkout for approval {}", event.approvalId());
        executeCheckout(event.approvalId(), event.userId());
    }
}
