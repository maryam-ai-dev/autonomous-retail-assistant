package com.aisleon.basket.application;

import com.aisleon.basket.BasketItemNotFoundException;
import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.FlagNotPresentException;
import com.aisleon.basket.SubstitutionAcceptedEvent;
import com.aisleon.basket.infrastructure.BasketItemJpaEntity;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BasketFlagService {

    private final BasketRepository basketRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BasketFlagService(
            BasketRepository basketRepository, ApplicationEventPublisher eventPublisher) {
        this.basketRepository = basketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BasketJpaEntity resolveFlag(UUID basketId, UUID itemId) {
        BasketJpaEntity basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new BasketNotFoundException(basketId));
        BasketItemJpaEntity target = null;
        for (BasketItemJpaEntity item : basket.getItems()) {
            if (itemId.equals(item.getId())) {
                target = item;
                break;
            }
        }
        if (target == null) {
            throw new BasketItemNotFoundException(basketId, itemId);
        }
        if (target.getSubstitutionFlagType() == null) {
            throw new FlagNotPresentException(itemId);
        }
        String flagType = target.getSubstitutionFlagType();
        target.setSubstitutionFlagResolved(true);
        basket.setUpdatedAt(LocalDateTime.now());
        BasketJpaEntity saved = basketRepository.save(basket);
        eventPublisher.publishEvent(new SubstitutionAcceptedEvent(
                basket.getId(),
                itemId,
                basket.getUserId(),
                flagType,
                Instant.now()));
        return saved;
    }
}
