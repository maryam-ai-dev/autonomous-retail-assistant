package com.aisleon.basket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.basket.BasketItemNotFoundException;
import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.FlagNotPresentException;
import com.aisleon.basket.SubstitutionAcceptedEvent;
import com.aisleon.basket.infrastructure.BasketItemJpaEntity;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class BasketFlagServiceTest {

    private final BasketRepository basketRepo = mock(BasketRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);

    private final BasketFlagService service = new BasketFlagService(basketRepo, events);

    @Test
    void resolvesFlagOnlyOnTargetItem() {
        UUID target = UUID.randomUUID();
        UUID untouched = UUID.randomUUID();
        BasketItemJpaEntity targetItem = flaggedItem(target);
        BasketItemJpaEntity otherItem = flaggedItem(untouched);
        BasketJpaEntity basket = BasketJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .basketIntentId(UUID.randomUUID())
                .items(new java.util.ArrayList<>(List.of(targetItem, otherItem)))
                .retailersUsed(List.of("TESCO"))
                .build();
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));
        when(basketRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.resolveFlag(basket.getId(), target);

        assertThat(targetItem.getSubstitutionFlagResolved()).isTrue();
        assertThat(otherItem.getSubstitutionFlagResolved()).isFalse();
        verify(events).publishEvent(any(SubstitutionAcceptedEvent.class));
    }

    @Test
    void rejectsWhenItemHasNoFlag() {
        UUID itemId = UUID.randomUUID();
        BasketItemJpaEntity plainItem = BasketItemJpaEntity.builder()
                .id(itemId)
                .externalProductId("p")
                .retailer(Retailer.TESCO)
                .canonicalName("x")
                .displayName("x")
                .price(new BigDecimal("1"))
                .quantity(1)
                .dietaryTags(List.of())
                .substitutionFlagResolved(false)
                .build();
        BasketJpaEntity basket = BasketJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .basketIntentId(UUID.randomUUID())
                .items(new java.util.ArrayList<>(List.of(plainItem)))
                .retailersUsed(List.of())
                .build();
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));

        assertThatThrownBy(() -> service.resolveFlag(basket.getId(), itemId))
                .isInstanceOf(FlagNotPresentException.class);
    }

    @Test
    void notFoundWhenBasketMissing() {
        UUID missing = UUID.randomUUID();
        when(basketRepo.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveFlag(missing, UUID.randomUUID()))
                .isInstanceOf(BasketNotFoundException.class);
    }

    @Test
    void notFoundWhenItemMissingInBasket() {
        BasketJpaEntity basket = BasketJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .basketIntentId(UUID.randomUUID())
                .items(new java.util.ArrayList<>())
                .retailersUsed(List.of())
                .build();
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));

        assertThatThrownBy(() -> service.resolveFlag(basket.getId(), UUID.randomUUID()))
                .isInstanceOf(BasketItemNotFoundException.class);
    }

    private BasketItemJpaEntity flaggedItem(UUID id) {
        return BasketItemJpaEntity.builder()
                .id(id)
                .externalProductId("p")
                .retailer(Retailer.TESCO)
                .canonicalName("x")
                .displayName("x")
                .price(new BigDecimal("1"))
                .quantity(1)
                .dietaryTags(List.of())
                .substitutionFlagType("BRAND_CHANGED")
                .substitutionFlagReason("Brand changed")
                .substitutionFlagResolved(false)
                .build();
    }
}
