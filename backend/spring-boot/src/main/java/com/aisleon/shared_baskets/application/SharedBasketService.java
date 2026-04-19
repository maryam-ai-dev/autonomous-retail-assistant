package com.aisleon.shared_baskets.application;

import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
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
    private final ShareIdGenerator shareIdGenerator;

    public SharedBasketService(
            SharedBasketRepository sharedRepo,
            BasketRepository basketRepo,
            ShareIdGenerator shareIdGenerator) {
        this.sharedRepo = sharedRepo;
        this.basketRepo = basketRepo;
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
}
