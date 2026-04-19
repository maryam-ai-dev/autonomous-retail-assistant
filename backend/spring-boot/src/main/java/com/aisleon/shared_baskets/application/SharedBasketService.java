package com.aisleon.shared_baskets.application;

import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.ClothingProfile;
import com.aisleon.basket.TasteProfile;
import com.aisleon.basket.application.BasketOrchestrationService;
import com.aisleon.basket.infrastructure.BasketIntentJpaEntity;
import com.aisleon.basket.infrastructure.BasketIntentRepository;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.shared_baskets.SharedBasketNotFoundException;
import com.aisleon.shared_baskets.infrastructure.SharedBasketJpaEntity;
import com.aisleon.shared_baskets.infrastructure.SharedBasketRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SharedBasketService {

    private final SharedBasketRepository sharedRepo;
    private final BasketRepository basketRepo;
    private final BasketIntentRepository intentRepo;
    private final BasketOrchestrationService orchestration;
    private final ShareIdGenerator shareIdGenerator;

    public SharedBasketService(
            SharedBasketRepository sharedRepo,
            BasketRepository basketRepo,
            BasketIntentRepository intentRepo,
            BasketOrchestrationService orchestration,
            ShareIdGenerator shareIdGenerator) {
        this.sharedRepo = sharedRepo;
        this.basketRepo = basketRepo;
        this.intentRepo = intentRepo;
        this.orchestration = orchestration;
        this.shareIdGenerator = shareIdGenerator;
    }

    @Transactional
    public SharedBasketJpaEntity share(UUID basketId) {
        return sharedRepo.findByBasketId(basketId).orElseGet(() -> {
            BasketJpaEntity basket = basketRepo.findById(basketId)
                    .orElseThrow(() -> new BasketNotFoundException(basketId));
            SharedBasketJpaEntity entity = SharedBasketJpaEntity.builder()
                    .basketId(basketId)
                    .ownerUserId(basket.getUserId())
                    .shareId(shareIdGenerator.generate())
                    .createdAt(LocalDateTime.now())
                    .build();
            return sharedRepo.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public SharedBasketJpaEntity findByShareId(String shareId) {
        return sharedRepo.findByShareId(shareId)
                .orElseThrow(() -> new SharedBasketNotFoundException(shareId));
    }

    @Transactional(readOnly = true)
    public BasketJpaEntity loadBasketByShareId(String shareId) {
        SharedBasketJpaEntity shared = findByShareId(shareId);
        return basketRepo.findById(shared.getBasketId())
                .orElseThrow(() -> new BasketNotFoundException(shared.getBasketId()));
    }

    /**
     * Forks a shared basket: creates a brand new basket-intent submission for the
     * calling user using the original basket's intent text, then runs the full
     * generation flow against the caller's own taste and clothing profile.
     */
    @Transactional
    public BasketJpaEntity fork(
            String shareId,
            UUID forkerUserId,
            TasteProfile forkerTaste,
            ClothingProfile forkerClothing) {
        SharedBasketJpaEntity shared = findByShareId(shareId);
        BasketJpaEntity originalBasket = basketRepo.findById(shared.getBasketId())
                .orElseThrow(() -> new BasketNotFoundException(shared.getBasketId()));
        BasketIntentJpaEntity originalIntent =
                intentRepo.findById(originalBasket.getBasketIntentId())
                        .orElseThrow(() -> new BasketNotFoundException(
                                originalBasket.getBasketIntentId()));
        return orchestration.submit(
                forkerUserId, originalIntent.getRawText(), forkerTaste, forkerClothing);
    }
}
