package com.aisleon.basket.application;

import com.aisleon.basket.BasketApprovedEvent;
import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.BasketStatus;
import com.aisleon.basket.BudgetExceededException;
import com.aisleon.basket.UnresolvedFlagsException;
import com.aisleon.basket.infrastructure.BasketIntentJpaEntity;
import com.aisleon.basket.infrastructure.BasketIntentRepository;
import com.aisleon.basket.infrastructure.BasketItemJpaEntity;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Approves a DRAFT basket after defensive re-validation. Spring is the final
 * authority here: even though B6.3 trimmed the basket, we check again.
 */
@Service
public class BasketApprovalService {

    private static final Logger log = LoggerFactory.getLogger(BasketApprovalService.class);

    private final BasketRepository basketRepository;
    private final BasketIntentRepository intentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BasketApprovalService(
            BasketRepository basketRepository,
            BasketIntentRepository intentRepository,
            ApplicationEventPublisher eventPublisher) {
        this.basketRepository = basketRepository;
        this.intentRepository = intentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BasketJpaEntity approve(UUID basketId) {
        BasketJpaEntity basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new BasketNotFoundException(basketId));

        int unresolved = countUnresolvedFlags(basket);
        if (unresolved > 0) {
            throw new UnresolvedFlagsException(unresolved);
        }

        BigDecimal total = BasketMapper.total(basket);
        BasketIntentJpaEntity intent = intentRepository.findById(basket.getBasketIntentId())
                .orElseThrow(() -> new BasketNotFoundException(basket.getBasketIntentId()));
        BigDecimal budget = intent.getBudget();
        if (budget != null && total.compareTo(budget) > 0) {
            throw new BudgetExceededException(total, budget);
        }

        basket.setStatus(BasketStatus.APPROVED);
        basket.setTotalCost(total);
        basket.setUpdatedAt(LocalDateTime.now());
        BasketJpaEntity saved = basketRepository.save(basket);

        List<Retailer> retailers = new ArrayList<>();
        for (String r : saved.getRetailersUsed()) {
            try {
                retailers.add(Retailer.valueOf(r));
            } catch (IllegalArgumentException ignored) {
                // legacy data — skip
            }
        }
        eventPublisher.publishEvent(new BasketApprovedEvent(
                saved.getId(),
                saved.getBasketIntentId(),
                saved.getUserId(),
                total,
                budget,
                retailers,
                Instant.now()));
        log.info(
                "BasketApproved: basketId={} userId={} total={} budget={}",
                saved.getId(),
                saved.getUserId(),
                total,
                budget);
        return saved;
    }

    private static int countUnresolvedFlags(BasketJpaEntity basket) {
        int count = 0;
        for (BasketItemJpaEntity item : basket.getItems()) {
            if (item.getSubstitutionFlagType() != null
                    && !Boolean.TRUE.equals(item.getSubstitutionFlagResolved())) {
                count++;
            }
        }
        return count;
    }
}
