package com.aisleon.basket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class BasketApprovalServiceTest {

    private final BasketRepository basketRepo = mock(BasketRepository.class);
    private final BasketIntentRepository intentRepo = mock(BasketIntentRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);

    private final BasketApprovalService service =
            new BasketApprovalService(basketRepo, intentRepo, events);

    @Test
    void approvesDraftAndEmitsEvent() {
        BasketJpaEntity basket = basket(BasketStatus.DRAFT, List.of(
                item("1.50", null, false),
                item("2.00", null, false)));
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));
        when(basketRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(intentRepo.findById(basket.getBasketIntentId()))
                .thenReturn(Optional.of(intentWithBudget(new BigDecimal("10.00"))));

        BasketJpaEntity result = service.approve(basket.getId());

        assertThat(result.getStatus()).isEqualTo(BasketStatus.APPROVED);
        verify(events).publishEvent(any(BasketApprovedEvent.class));
    }

    @Test
    void rejectsWhenUnresolvedFlagsPresent() {
        BasketJpaEntity basket = basket(BasketStatus.DRAFT, List.of(
                item("1.50", "BRAND_CHANGED", false),
                item("1.50", "BRAND_CHANGED", true)));
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));

        assertThatThrownBy(() -> service.approve(basket.getId()))
                .isInstanceOfSatisfying(
                        UnresolvedFlagsException.class,
                        ex -> assertThat(ex.unresolvedCount()).isEqualTo(1));
        verify(basketRepo, never()).save(any());
    }

    @Test
    void rejectsWhenTotalExceedsBudget() {
        BasketJpaEntity basket = basket(BasketStatus.DRAFT, List.of(
                item("40.00", null, false),
                item("35.00", null, false)));
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));
        when(intentRepo.findById(basket.getBasketIntentId()))
                .thenReturn(Optional.of(intentWithBudget(new BigDecimal("70.00"))));

        assertThatThrownBy(() -> service.approve(basket.getId()))
                .isInstanceOf(BudgetExceededException.class);
    }

    @Test
    void allowsApprovalWhenBudgetIsNullOnIntent() {
        BasketJpaEntity basket = basket(BasketStatus.DRAFT, List.of(item("5.00", null, false)));
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));
        when(basketRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(intentRepo.findById(basket.getBasketIntentId()))
                .thenReturn(Optional.of(intentWithBudget(null)));

        BasketJpaEntity result = service.approve(basket.getId());
        assertThat(result.getStatus()).isEqualTo(BasketStatus.APPROVED);
    }

    @Test
    void notFoundWhenBasketMissing() {
        UUID missing = UUID.randomUUID();
        when(basketRepo.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(missing))
                .isInstanceOf(BasketNotFoundException.class);
    }

    private BasketJpaEntity basket(BasketStatus status, List<BasketItemJpaEntity> items) {
        UUID id = UUID.randomUUID();
        BasketJpaEntity basket = BasketJpaEntity.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .basketIntentId(UUID.randomUUID())
                .status(status)
                .items(new ArrayList<>(items))
                .retailersUsed(List.of(Retailer.TESCO.name()))
                .totalCost(BigDecimal.ZERO)
                .build();
        return basket;
    }

    private BasketIntentJpaEntity intentWithBudget(BigDecimal budget) {
        return BasketIntentJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .rawText("x")
                .budget(budget)
                .build();
    }

    private BasketItemJpaEntity item(String price, String flagType, boolean resolved) {
        return BasketItemJpaEntity.builder()
                .externalProductId("p" + UUID.randomUUID())
                .retailer(Retailer.TESCO)
                .canonicalName("x")
                .displayName("x")
                .price(new BigDecimal(price))
                .quantity(1)
                .substitutionFlagType(flagType)
                .substitutionFlagResolved(resolved)
                .dietaryTags(List.of())
                .build();
    }
}
