package com.aisleon.shared_baskets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.basket.BasketNotFoundException;
import com.aisleon.basket.infrastructure.BasketJpaEntity;
import com.aisleon.basket.infrastructure.BasketRepository;
import com.aisleon.shared_baskets.infrastructure.SharedBasketJpaEntity;
import com.aisleon.shared_baskets.infrastructure.SharedBasketRepository;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SharedBasketServiceTest {

    private final SharedBasketRepository sharedRepo = mock(SharedBasketRepository.class);
    private final BasketRepository basketRepo = mock(BasketRepository.class);
    private final ShareIdGenerator gen = mock(ShareIdGenerator.class);
    private final SharedBasketService service =
            new SharedBasketService(sharedRepo, basketRepo, gen);

    @Test
    void firstShareCreatesNewShareId() {
        UUID basketId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(sharedRepo.findByBasketId(basketId)).thenReturn(Optional.empty());
        when(basketRepo.findById(basketId)).thenReturn(Optional.of(basket(basketId, ownerId)));
        when(gen.generate()).thenReturn("ABC12345");
        when(sharedRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SharedBasketJpaEntity out = service.share(basketId);
        assertThat(out.getShareId()).isEqualTo("ABC12345");
        assertThat(out.getBasketId()).isEqualTo(basketId);
        assertThat(out.getOwnerUserId()).isEqualTo(ownerId);
    }

    @Test
    void secondShareReturnsExistingShareId() {
        UUID basketId = UUID.randomUUID();
        SharedBasketJpaEntity existing = SharedBasketJpaEntity.builder()
                .id(UUID.randomUUID())
                .basketId(basketId)
                .ownerUserId(UUID.randomUUID())
                .shareId("STABLE01")
                .build();
        when(sharedRepo.findByBasketId(basketId)).thenReturn(Optional.of(existing));

        SharedBasketJpaEntity out = service.share(basketId);
        assertThat(out.getShareId()).isEqualTo("STABLE01");
        verify(gen, never()).generate();
        verify(sharedRepo, never()).save(any());
    }

    @Test
    void notFoundWhenBasketMissing() {
        UUID basketId = UUID.randomUUID();
        when(sharedRepo.findByBasketId(basketId)).thenReturn(Optional.empty());
        when(basketRepo.findById(basketId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.share(basketId))
                .isInstanceOf(BasketNotFoundException.class);
    }

    private BasketJpaEntity basket(UUID id, UUID ownerId) {
        return BasketJpaEntity.builder()
                .id(id)
                .userId(ownerId)
                .basketIntentId(UUID.randomUUID())
                .items(new ArrayList<>())
                .retailersUsed(new ArrayList<>())
                .build();
    }
}
