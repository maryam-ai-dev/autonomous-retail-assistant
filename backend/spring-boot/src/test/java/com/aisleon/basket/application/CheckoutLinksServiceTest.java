package com.aisleon.basket.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CheckoutLinksServiceTest {

    private final BasketRepository basketRepo = mock(BasketRepository.class);
    private final RetailerCheckoutUrlService urlService = new RetailerCheckoutUrlService(
            "https://tesco.example/cart",
            "https://sainsburys.example/cart",
            "https://boots.example/cart",
            "https://argos.example/cart");

    private final CheckoutLinksService service = new CheckoutLinksService(basketRepo, urlService);

    @Test
    void returnsOneUrlPerKnownRetailerUsed() {
        BasketJpaEntity basket = basket(List.of("TESCO", "SAINSBURYS"));
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));

        var links = service.linksFor(basket.getId());

        assertThat(links)
                .containsOnlyKeys("TESCO", "SAINSBURYS")
                .containsEntry("TESCO", "https://tesco.example/cart")
                .containsEntry("SAINSBURYS", "https://sainsburys.example/cart");
    }

    @Test
    void skipsUnknownRetailerInsteadOfFailing() {
        BasketJpaEntity basket = basket(List.of("TESCO", "OLDMORRISONS"));
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));

        var links = service.linksFor(basket.getId());
        assertThat(links).containsOnlyKeys("TESCO");
    }

    @Test
    void emptyWhenNoRetailers() {
        BasketJpaEntity basket = basket(List.of());
        when(basketRepo.findById(basket.getId())).thenReturn(Optional.of(basket));
        assertThat(service.linksFor(basket.getId())).isEmpty();
    }

    @Test
    void notFoundWhenBasketMissing() {
        UUID missing = UUID.randomUUID();
        when(basketRepo.findById(missing)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.linksFor(missing))
                .isInstanceOf(BasketNotFoundException.class);
    }

    private BasketJpaEntity basket(List<String> retailers) {
        return BasketJpaEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .basketIntentId(UUID.randomUUID())
                .items(new ArrayList<>())
                .retailersUsed(new ArrayList<>(retailers))
                .build();
    }
}
