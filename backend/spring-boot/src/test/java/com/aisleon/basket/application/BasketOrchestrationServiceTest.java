package com.aisleon.basket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.basket.BasketConstraintEngine;
import com.aisleon.basket.BasketGeneratedEvent;
import com.aisleon.basket.BasketStatus;
import com.aisleon.basket.BudgetTooLowForIntentException;
import com.aisleon.basket.CandidatePool;
import com.aisleon.basket.CandidateSelectionService;
import com.aisleon.basket.ClothingProfile;
import com.aisleon.basket.ClothingProfileGate;
import com.aisleon.basket.ParsedIntent;
import com.aisleon.basket.TasteProfile;
import com.aisleon.basket.bridge.AiServiceBridgeClient;
import com.aisleon.basket.bridge.AiServiceBridgeClient.GeneratedDraftDto;
import com.aisleon.basket.bridge.AiServiceBridgeClient.GeneratedItemDto;
import com.aisleon.basket.infrastructure.BasketIntentJpaEntity;
import com.aisleon.basket.infrastructure.BasketIntentRepository;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.catalogue.CertificationTag;
import com.aisleon.catalogue.DietaryTag;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.OfferFlag;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class BasketOrchestrationServiceTest {

    private final AiServiceBridgeClient aiBridge = mock(AiServiceBridgeClient.class);
    private final ClothingProfileGate gate = mock(ClothingProfileGate.class);
    private final CandidateSelectionService selection = mock(CandidateSelectionService.class);
    private final BasketConstraintEngine engine = new BasketConstraintEngine();
    private final BasketIntentRepository intentRepo = mock(BasketIntentRepository.class);
    private final BasketRepository basketRepo = mock(BasketRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);

    private final BasketOrchestrationService service =
            new BasketOrchestrationService(
                    aiBridge, gate, selection, engine, intentRepo, basketRepo, events);

    private final UUID userId = UUID.randomUUID();

    @Test
    void endToEndPersistsAsDraftAndPublishesEvent() {
        NormalizedProduct milk = product("m", "oat milk", new BigDecimal("1.80"), List.of());
        NormalizedProduct bread = product("b", "bread", new BigDecimal("1.20"), List.of());
        stubIntent(new BigDecimal("70"));
        stubCandidates(List.of(milk, bread));
        stubDraft(List.of(item("m", 1), item("b", 2)), "3.60", 0);
        stubIntentSave();
        stubBasketSave();
        stubNoPriorBasket();

        BasketJpaEntity saved = service.submit(userId, "weekly groceries under £70", TasteProfile.empty(), null);

        assertThat(saved.getStatus()).isEqualTo(BasketStatus.DRAFT);
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getTotalCost()).isEqualByComparingTo("4.20");
        assertThat(saved.getTrimmed()).isFalse();
        ArgumentCaptor<BasketGeneratedEvent> ev = ArgumentCaptor.forClass(BasketGeneratedEvent.class);
        verify(events).publishEvent(ev.capture());
        assertThat(ev.getValue().itemCount()).isEqualTo(2);
    }

    @Test
    void draftOverBudgetIsTrimmedBySpring() {
        // £70 budget. FastAPI proposes £74 (40 + 34). Spring should trim.
        NormalizedProduct expensive = product("e", "salmon", new BigDecimal("40.00"), List.of());
        NormalizedProduct mid = product("m", "beef", new BigDecimal("34.00"), List.of());
        stubIntent(new BigDecimal("70"));
        stubCandidates(List.of(expensive, mid));
        stubDraft(List.of(item("e", 1), item("m", 1)), "74.00", 0);
        stubIntentSave();
        stubBasketSave();
        stubNoPriorBasket();

        BasketJpaEntity saved = service.submit(userId, "some dinner", TasteProfile.empty(), null);

        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getTotalCost()).isLessThanOrEqualTo(new BigDecimal("70.00"));
        assertThat(saved.getTrimmed()).isTrue();
        assertThat(saved.getTrimmedItemCount()).isEqualTo(1);
    }

    @Test
    void budgetTooLowThrowsUnprocessableEntitySemantics() {
        // Budget £10 but every candidate costs £40 → trim would empty → 422.
        NormalizedProduct pricey = product("p", "expensive", new BigDecimal("40.00"), List.of());
        stubIntent(new BigDecimal("10"));
        stubCandidates(List.of(pricey));
        stubDraft(List.of(item("p", 1)), "40.00", 0);
        stubIntentSave();
        stubBasketSave();
        stubNoPriorBasket();

        assertThatThrownBy(() -> service.submit(userId, "anything", TasteProfile.empty(), null))
                .isInstanceOf(BudgetTooLowForIntentException.class);

        verify(basketRepo, never()).save(any());
    }

    @Test
    void substitutionFlagDetectedWhenBrandChangesFromPreviousBasket() {
        NormalizedProduct tescoMilk = productBranded("m", "oat milk", "Tesco",
                new BigDecimal("1.50"));
        stubIntent(new BigDecimal("50"));
        stubCandidates(List.of(tescoMilk));
        stubDraft(List.of(item("m", 1)), "1.50", 0);
        stubIntentSave();
        stubBasketSave();
        // Prior approved basket has same canonical name but different brand → substitution.
        BasketJpaEntity priorBasket = BasketJpaEntity.builder()
                .status(BasketStatus.APPROVED)
                .items(new ArrayList<>())
                .build();
        com.aisleon.basket.infrastructure.BasketItemJpaEntity priorItem =
                com.aisleon.basket.infrastructure.BasketItemJpaEntity.builder()
                        .externalProductId("old-m")
                        .retailer(Retailer.SAINSBURYS)
                        .canonicalName("oat milk")
                        .displayName("oat milk")
                        .brand("Alpro")
                        .price(new BigDecimal("1.50"))
                        .quantity(1)
                        .dietaryTags(List.of())
                        .build();
        priorBasket.getItems().add(priorItem);
        when(basketRepo.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                        userId, BasketStatus.APPROVED))
                .thenReturn(Optional.of(priorBasket));

        BasketJpaEntity saved = service.submit(userId, "x", TasteProfile.empty(), null);

        assertThat(saved.getItems().get(0).getSubstitutionFlagType())
                .isEqualTo("BRAND_CHANGED");
    }

    @Test
    void clothingGateShortCircuitsForFashionIntent() {
        when(aiBridge.parseIntent(anyString()))
                .thenReturn(new ParsedIntent("dress", new BigDecimal("60"),
                        ProductCategory.FASHION, List.of(), false, List.of()));
        org.mockito.Mockito.doThrow(new com.aisleon.basket.ClothingProfileIncompleteException())
                .when(gate).assertReadyFor(any(), eq(userId));

        assertThatThrownBy(() -> service.submit(userId, "a dress", TasteProfile.empty(), null))
                .isInstanceOf(com.aisleon.basket.ClothingProfileIncompleteException.class);

        verify(selection, never()).select(any(), any(), any(), anyInt());
    }

    // ---- helpers -----------------------------------------------------------

    private void stubIntent(BigDecimal budget) {
        when(aiBridge.parseIntent(anyString()))
                .thenReturn(new ParsedIntent(
                        "raw", budget, ProductCategory.GROCERY, List.of(), false, List.of()));
    }

    private void stubCandidates(List<NormalizedProduct> products) {
        when(selection.select(any(), any(), any(), anyInt()))
                .thenReturn(new CandidatePool(products, Map.of()));
    }

    private void stubDraft(List<GeneratedItemDto> items, String total, int retryCount) {
        when(aiBridge.generateBasket(any()))
                .thenReturn(new GeneratedDraftDto(
                        items, new BigDecimal(total), retryCount, List.of()));
    }

    private void stubIntentSave() {
        when(intentRepo.save(any())).thenAnswer(inv -> {
            BasketIntentJpaEntity arg = inv.getArgument(0);
            if (arg.getId() == null) arg.setId(UUID.randomUUID());
            return arg;
        });
    }

    private void stubBasketSave() {
        when(basketRepo.save(any())).thenAnswer(inv -> {
            BasketJpaEntity arg = inv.getArgument(0);
            if (arg.getId() == null) arg.setId(UUID.randomUUID());
            return arg;
        });
    }

    private void stubNoPriorBasket() {
        when(basketRepo.findTopByUserIdAndStatusOrderByCreatedAtDesc(
                        any(), eq(BasketStatus.APPROVED)))
                .thenReturn(Optional.empty());
    }

    private static GeneratedItemDto item(String id, int qty) {
        return new GeneratedItemDto(id, qty, "because");
    }

    private static NormalizedProduct product(
            String id, String name, BigDecimal price, List<DietaryTag> dietary) {
        return productBranded(id, name, "brand", price);
    }

    private static NormalizedProduct productBranded(
            String id, String name, String brand, BigDecimal price) {
        return new NormalizedProduct(
                id, name, name, brand, Retailer.TESCO,
                ProductCategory.GROCERY, ProductSubcategory.DAIRY,
                price, null, null, null, "https://img", "https://p/" + id,
                true, true,
                List.<DietaryTag>of(),
                List.<CertificationTag>of(),
                List.<OfferFlag>of(),
                0.9, Instant.now(), List.of(), List.of());
    }
}
