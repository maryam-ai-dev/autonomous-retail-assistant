package com.aisleon.basket.application;

import com.aisleon.basket.BasketConstraintEngine;
import com.aisleon.basket.BasketGeneratedEvent;
import com.aisleon.basket.BasketItem;
import com.aisleon.basket.BasketStatus;
import com.aisleon.basket.BudgetTooLowException;
import com.aisleon.basket.BudgetTooLowForIntentException;
import com.aisleon.basket.CandidatePool;
import com.aisleon.basket.CandidateSelectionService;
import com.aisleon.basket.ClothingProfile;
import com.aisleon.basket.ClothingProfileGate;
import com.aisleon.basket.ParsedIntent;
import com.aisleon.basket.SubstitutionFlag;
import com.aisleon.basket.TasteProfile;
import com.aisleon.basket.bridge.AiBridgeException;
import com.aisleon.basket.bridge.AiServiceBridgeClient;
import com.aisleon.basket.bridge.AiServiceBridgeClient.BasketGenerateRequestDto;
import com.aisleon.basket.bridge.AiServiceBridgeClient.GeneratedDraftDto;
import com.aisleon.basket.bridge.AiServiceBridgeClient.GeneratedItemDto;
import com.aisleon.basket.infrastructure.BasketIntentJpaEntity;
import com.aisleon.basket.infrastructure.BasketIntentRepository;
import com.aisleon.basket.infrastructure.BasketItemJpaEntity;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the basket-generation flow end-to-end.
 *
 * <p>Spring is the final authority here — regardless of what FastAPI proposes,
 * the persisted basket is re-validated, trimmed if necessary, and persisted as
 * a DRAFT with substitution flags detected against the user's previous basket.
 */
@Service
public class BasketOrchestrationService {

    private static final Logger log =
            LoggerFactory.getLogger(BasketOrchestrationService.class);
    private static final int MAX_CANDIDATES_PER_RETAILER = 20;

    private final AiServiceBridgeClient aiBridge;
    private final ClothingProfileGate clothingGate;
    private final CandidateSelectionService candidateSelection;
    private final BasketConstraintEngine engine;
    private final BasketIntentRepository intentRepository;
    private final BasketRepository basketRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BasketOrchestrationService(
            AiServiceBridgeClient aiBridge,
            ClothingProfileGate clothingGate,
            CandidateSelectionService candidateSelection,
            BasketConstraintEngine engine,
            BasketIntentRepository intentRepository,
            BasketRepository basketRepository,
            ApplicationEventPublisher eventPublisher) {
        this.aiBridge = aiBridge;
        this.clothingGate = clothingGate;
        this.candidateSelection = candidateSelection;
        this.engine = engine;
        this.intentRepository = intentRepository;
        this.basketRepository = basketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BasketJpaEntity submit(
            UUID userId,
            String rawText,
            TasteProfile taste,
            ClothingProfile clothing) {
        // 1. Parse intent via FastAPI
        ParsedIntent intent = aiBridge.parseIntent(rawText);

        // 2. Clothing-profile gate for fashion intents
        clothingGate.assertReadyFor(intent, userId);

        // 3. Candidate selection
        CandidatePool pool = candidateSelection.select(
                intent, taste, clothing, MAX_CANDIDATES_PER_RETAILER);

        // 4. Advisory basket generation via FastAPI
        GeneratedDraftDto draft;
        try {
            draft = aiBridge.generateBasket(
                    new BasketGenerateRequestDto(intent, pool.products()));
        } catch (AiBridgeException e) {
            log.warn("basket generate bridge error: {}", e.getMessage());
            throw e;
        }

        // 5. Spring final authority validation + trimming
        Map<String, NormalizedProduct> byId = new HashMap<>();
        for (NormalizedProduct p : pool.products()) byId.put(p.externalId(), p);

        List<BasketItem> items = toBasketItems(draft, byId);

        boolean trimmed = false;
        int trimmedItemCount = 0;
        if (intent.budget() != null) {
            List<BasketItem> original = items;
            try {
                items = engine.trimToBudget(items, intent.budget());
            } catch (BudgetTooLowException ex) {
                log.warn(
                        "BasketBudgetTrimmed: budget={} items_dropped_all — returning 422",
                        intent.budget());
                throw new BudgetTooLowForIntentException(
                        "BUDGET_TOO_LOW_FOR_INTENT — budget=" + intent.budget());
            }
            if (items.size() < original.size()) {
                trimmed = true;
                trimmedItemCount = original.size() - items.size();
                log.warn(
                        "BasketBudgetTrimmed: fastapi_total={} budget={} trimmed_items={}",
                        draft.totalCost,
                        intent.budget(),
                        trimmedItemCount);
            }
        }

        // Substitution detection vs user's last basket
        List<BasketItem> previous = loadPreviousItems(userId);
        List<SubstitutionFlag> flags = engine.detectSubstitutions(items, previous);
        items = applyFlags(items, flags);

        // 6. Persist intent + basket (DRAFT)
        BasketIntentJpaEntity intentEntity = intentRepository.save(toIntentEntity(userId, intent));
        BasketJpaEntity basket = persistBasket(
                userId, intentEntity, items, draft, trimmed, trimmedItemCount);

        BigDecimal total = BasketMapper.total(basket);
        log.info(
                "BasketGenerated: intentId={} retailersUsed={} itemCount={} total={} budget={} trimmed={}",
                intentEntity.getId(),
                basket.getRetailersUsed(),
                basket.getItems().size(),
                total,
                intent.budget(),
                trimmed);
        eventPublisher.publishEvent(new BasketGeneratedEvent(
                basket.getId(),
                intentEntity.getId(),
                userId,
                basket.getRetailersUsed().stream()
                        .map(Retailer::valueOf)
                        .toList(),
                basket.getItems().size(),
                total,
                intent.budget(),
                trimmed,
                trimmedItemCount));
        return basket;
    }

    private BasketIntentJpaEntity toIntentEntity(UUID userId, ParsedIntent intent) {
        return BasketIntentJpaEntity.builder()
                .userId(userId)
                .rawText(intent.rawText())
                .budget(intent.budget())
                .currency("GBP")
                .category(intent.primaryCategory())
                .tags(new ArrayList<>(intent.tags()))
                .halalRequired(intent.halalRequired())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private BasketJpaEntity persistBasket(
            UUID userId,
            BasketIntentJpaEntity intent,
            List<BasketItem> items,
            GeneratedDraftDto draft,
            boolean trimmed,
            int trimmedItemCount) {
        BasketJpaEntity basket = BasketJpaEntity.builder()
                .userId(userId)
                .basketIntentId(intent.getId())
                .status(BasketStatus.DRAFT)
                .totalCost(BigDecimal.ZERO)
                .currency("GBP")
                .retailersUsed(retailersOf(items))
                .trimmed(trimmed)
                .trimmedItemCount(trimmedItemCount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Map<String, String> reasoningByCandidate = new HashMap<>();
        for (GeneratedItemDto gi : draft.items) {
            reasoningByCandidate.put(gi.candidateId, gi.reasoning);
        }
        for (BasketItem item : items) {
            String reasoning = reasoningByCandidate.getOrDefault(
                    item.product().externalId(), "");
            BasketItemJpaEntity jpa = BasketMapper.toJpa(item, reasoning);
            jpa.setBasket(basket);
            basket.getItems().add(jpa);
        }
        basket.setTotalCost(BasketMapper.total(basket));
        return basketRepository.save(basket);
    }

    private static List<String> retailersOf(List<BasketItem> items) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (BasketItem item : items) set.add(item.product().retailer().name());
        return new ArrayList<>(set);
    }

    private List<BasketItem> toBasketItems(
            GeneratedDraftDto draft, Map<String, NormalizedProduct> byId) {
        List<BasketItem> out = new ArrayList<>();
        for (GeneratedItemDto entry : draft.items) {
            NormalizedProduct product = byId.get(entry.candidateId);
            if (product == null) continue; // defensive — already filtered by FastAPI
            out.add(new BasketItem(
                    UUID.randomUUID().toString(),
                    product,
                    Math.max(1, entry.quantity),
                    Optional.empty()));
        }
        return out;
    }

    private static List<BasketItem> applyFlags(
            List<BasketItem> items, List<SubstitutionFlag> flags) {
        if (flags.isEmpty()) return items;
        List<BasketItem> out = new ArrayList<>(items.size());
        int i = 0;
        for (BasketItem item : items) {
            SubstitutionFlag applied = i < flags.size() ? flags.get(i) : null;
            out.add(new BasketItem(
                    item.id(),
                    item.product(),
                    item.quantity(),
                    Optional.ofNullable(applied)));
            i++;
        }
        return out;
    }

    private List<BasketItem> loadPreviousItems(UUID userId) {
        return basketRepository
                .findTopByUserIdAndStatusOrderByCreatedAtDesc(userId, BasketStatus.APPROVED)
                .map(b -> b.getItems().stream().map(BasketMapper::fromJpa).toList())
                .orElse(List.of());
    }
}
