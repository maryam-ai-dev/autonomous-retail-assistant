package com.aisleon.shared_baskets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aisleon.shared_baskets.infrastructure.SharedBasketRepository;
import org.junit.jupiter.api.Test;

class ShareIdGeneratorTest {

    private final SharedBasketRepository repo = mock(SharedBasketRepository.class);
    private final ShareIdGenerator gen = new ShareIdGenerator(repo);

    @Test
    void generatedIdIsEightAlphanumericChars() {
        when(repo.existsByShareId(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        String out = gen.generate();
        assertThat(out).hasSize(8).matches("[A-Za-z0-9]+");
    }

    @Test
    void retriesOnCollisionThenYields() {
        when(repo.existsByShareId(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true, true, false);
        String out = gen.generate();
        assertThat(out).hasSize(8);
    }
}
