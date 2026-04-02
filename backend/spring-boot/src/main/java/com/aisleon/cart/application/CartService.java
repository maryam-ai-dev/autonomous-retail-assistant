package com.aisleon.cart.application;

import com.aisleon.cart.domain.Cart;
import com.aisleon.cart.domain.CartItem;
import com.aisleon.cart.domain.CartStatus;
import com.aisleon.cart.events.ApprovalRequiredEvent;
import com.aisleon.cart.events.CartCheckedOutEvent;
import com.aisleon.cart.events.CartItemAddedEvent;
import com.aisleon.cart.infrastructure.CartJpaEntity;
import com.aisleon.cart.infrastructure.CartMapper;
import com.aisleon.cart.infrastructure.CartRepository;
import com.aisleon.cart.interfaces.AddCartItemRequest;
import com.aisleon.merchant.infrastructure.MerchantJpaEntity;
import com.aisleon.merchant.infrastructure.MerchantRepository;
import com.aisleon.policy.domain.PolicyEvaluationResult;
import com.aisleon.policy.domain.PolicyEvaluationService;
import com.aisleon.policy.domain.PurchasePolicy;
import com.aisleon.preferences.domain.RetailPreferences;
import com.aisleon.preferences.infrastructure.RetailPreferencesMapper;
import com.aisleon.preferences.infrastructure.RetailPreferencesRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final RetailPreferencesRepository preferencesRepository;
    private final MerchantRepository merchantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PolicyEvaluationService policyEvaluationService;

    public CartService(CartRepository cartRepository,
                       RetailPreferencesRepository preferencesRepository,
                       MerchantRepository merchantRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.preferencesRepository = preferencesRepository;
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
        this.policyEvaluationService = new PolicyEvaluationService();
    }

    public Cart getOrCreateCart(UUID userId) {
        CartJpaEntity entity = cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> {
                    CartJpaEntity newCart = CartJpaEntity.builder()
                            .userId(userId)
                            .status("ACTIVE")
                            .build();
                    return cartRepository.save(newCart);
                });
        return CartMapper.toDomain(entity);
    }

    @Transactional
    public Cart addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = new CartItem(
                UUID.randomUUID(),
                request.getExternalProductId(),
                request.getTitle(),
                request.getPrice(),
                request.getCurrency() != null ? request.getCurrency() : "USD",
                request.getMerchantId(),
                request.getMerchantName(),
                request.getMerchantRating(),
                request.getSourceType(),
                request.getSourceName(),
                request.getProductUrl(),
                request.getImageUrl(),
                false,
                null,
                LocalDateTime.now()
        );

        cart.addItem(item);

        CartJpaEntity entity = CartMapper.toEntity(cart);
        entity.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(entity);

        eventPublisher.publishEvent(new CartItemAddedEvent(
                cart.getId(), userId, request.getExternalProductId(),
                request.getTitle(), request.getPrice(), request.getMerchantName()
        ));

        return CartMapper.toDomain(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public Cart removeItem(UUID userId, UUID itemId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(itemId);

        CartJpaEntity entity = CartMapper.toEntity(cart);
        entity.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(entity);

        return CartMapper.toDomain(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public void clearCart(UUID userId) {
        cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .ifPresent(entity -> {
                    entity.getItems().clear();
                    entity.setUpdatedAt(LocalDateTime.now());
                    cartRepository.save(entity);
                });
    }

    @Transactional
    public Map<String, Object> checkoutCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        RetailPreferences preferences = preferencesRepository.findByUserId(userId)
                .map(RetailPreferencesMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found"));

        PurchasePolicy policy = buildPolicy(preferences);
        PolicyEvaluationResult evaluation = policyEvaluationService.evaluate(cart, policy);

        if (!evaluation.isAllowed()) {
            return Map.of(
                    "status", "BLOCKED",
                    "reasons", evaluation.getBlockedReasons()
            );
        }

        if (evaluation.isRequiresApproval()) {
            String triggerReason = String.join("; ", evaluation.getWarnings());

            eventPublisher.publishEvent(new ApprovalRequiredEvent(
                    cart.getId(), userId, cart.getTotalAmount(),
                    triggerReason, evaluation.getWarnings()
            ));

            return Map.of(
                    "status", "APPROVAL_REQUIRED",
                    "triggerReason", triggerReason,
                    "warnings", evaluation.getWarnings()
            );
        }

        cart.setStatus(CartStatus.CHECKED_OUT);
        CartJpaEntity entity = CartMapper.toEntity(cart);
        entity.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(entity);

        eventPublisher.publishEvent(new CartCheckedOutEvent(
                cart.getId(), userId, cart.getTotalAmount(), cart.getItems().size()
        ));

        return Map.of(
                "status", "CHECKED_OUT",
                "totalAmount", cart.getTotalAmount()
        );
    }

    private PurchasePolicy buildPolicy(RetailPreferences preferences) {
        List<MerchantJpaEntity> approvedMerchants = merchantRepository.findAllByIsApproved(true);

        Set<UUID> approvedIds = approvedMerchants.stream()
                .map(MerchantJpaEntity::getId)
                .collect(Collectors.toSet());

        Set<String> approvedNames = approvedMerchants.stream()
                .map(MerchantJpaEntity::getName)
                .collect(Collectors.toSet());

        return new PurchasePolicy(
                preferences.getApprovalThreshold(),
                approvedIds,
                approvedNames,
                preferences.getAllowSubstitutions(),
                preferences.getMaxSubstitutionPriceDelta()
        );
    }
}
