package com.aisleon.shared_baskets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SharedBasketServiceTest {

    private final SharedBasketRepository sharedRepo = mock(SharedBasketRepository.class);
    private final BasketRepository basketRepo = mock(BasketRepository.class);
    private final BasketIntentRepository intentRepo = mock(BasketIntentRepository.class);
    private final BasketOrchestrationService orchestration = mock(BasketOrchestrationService.class);
    private final ShareIdGenerator gen = mock(ShareIdGenerator.class);
    private final SharedBasketService service =
            new SharedBasketService(sharedRepo, basketRepo, intentRepo, orchestration, gen);

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
    void shareNotFoundWhenBasketMissing() {
        UUID basketId = UUID.randomUUID();
        when(sharedRepo.findByBasketId(basketId)).thenReturn(Optional.empty());
        when(basketRepo.findById(basketId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.share(basketId))
                .isInstanceOf(BasketNotFoundException.class);
    }

    @Test
    void findByShareIdReturnsEntity() {
        SharedBasketJpaEntity existing = SharedBasketJpaEntity.builder()
                .id(UUID.randomUUID())
                .basketId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .shareId("LOOKUP01")
                .build();
        when(sharedRepo.findByShareId("LOOKUP01")).thenReturn(Optional.of(existing));

        SharedBasketJpaEntity out = service.findByShareId("LOOKUP01");
        assertThat(out).isSameAs(existing);
    }

    @Test
    void findByShareIdMissingThrows404() {
        when(sharedRepo.findByShareId("UNKNOWN1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByShareId("UNKNOWN1"))
                .isInstanceOf(SharedBasketNotFoundException.class);
    }

    @Test
    void loadBasketByShareIdReturnsBasket() {
        UUID basketId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        SharedBasketJpaEntity shared = SharedBasketJpaEntity.builder()
                .basketId(basketId)
                .ownerUserId(ownerId)
                .shareId("VIEW0001")
                .build();
        BasketJpaEntity basket = basket(basketId, ownerId);
        when(sharedRepo.findByShareId("VIEW0001")).thenReturn(Optional.of(shared));
        when(basketRepo.findById(basketId)).thenReturn(Optional.of(basket));

        BasketJpaEntity out = service.loadBasketByShareId("VIEW0001");
        assertThat(out).isSameAs(basket);
    }

    @Test
    void forkRunsOrchestrationForCallerUsingOriginalIntentText() {
        UUID basketId = UUID.randomUUID();
        UUID originalOwnerId = UUID.randomUUID();
        UUID forkerId = UUID.randomUUID();
        UUID intentId = UUID.randomUUID();
        UUID newBasketId = UUID.randomUUID();
        TasteProfile taste = new TasteProfile(false, false, false, List.of(), List.of(), List.of());
        ClothingProfile clothing = new ClothingProfile(null, null, null, null);

        SharedBasketJpaEntity shared = SharedBasketJpaEntity.builder()
                .basketId(basketId)
                .ownerUserId(originalOwnerId)
                .shareId("FORK0001")
                .build();
        BasketJpaEntity originalBasket = basket(basketId, originalOwnerId);
        originalBasket.setBasketIntentId(intentId);
        BasketIntentJpaEntity originalIntent = BasketIntentJpaEntity.builder()
                .id(intentId)
                .userId(originalOwnerId)
                .rawText("weekly halal groceries under £70")
                .build();
        BasketJpaEntity newBasket = basket(newBasketId, forkerId);

        when(sharedRepo.findByShareId("FORK0001")).thenReturn(Optional.of(shared));
        when(basketRepo.findById(basketId)).thenReturn(Optional.of(originalBasket));
        when(intentRepo.findById(intentId)).thenReturn(Optional.of(originalIntent));
        when(orchestration.submit(
                        eq(forkerId),
                        eq("weekly halal groceries under £70"),
                        eq(taste),
                        eq(clothing)))
                .thenReturn(newBasket);

        BasketJpaEntity out = service.fork("FORK0001", forkerId, taste, clothing);
        assertThat(out).isSameAs(newBasket);
        assertThat(out.getUserId())
                .as("forked basket owner is the caller, not the original owner")
                .isEqualTo(forkerId);
    }

    @Test
    void forkInvalidShareIdThrows404() {
        when(sharedRepo.findByShareId("MISSING1")).thenReturn(Optional.empty());

        TasteProfile taste = new TasteProfile(false, false, false, List.of(), List.of(), List.of());
        ClothingProfile clothing = new ClothingProfile(null, null, null, null);
        assertThatThrownBy(
                        () -> service.fork("MISSING1", UUID.randomUUID(), taste, clothing))
                .isInstanceOf(SharedBasketNotFoundException.class);
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
