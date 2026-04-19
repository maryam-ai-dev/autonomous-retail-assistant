package com.aisleon.basket;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aisleon.catalogue.ProductCategory;
import com.aisleon.preferences.application.TasteProfileService;
import com.aisleon.preferences.interfaces.ClothingProfileCompleteResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClothingProfileGateTest {

    private final TasteProfileService tasteProfileService = mock(TasteProfileService.class);
    private final ClothingProfileGate gate = new ClothingProfileGate(tasteProfileService);

    private final UUID userId = UUID.randomUUID();

    @Test
    void fashionIntentWithIncompleteProfileThrows() {
        when(tasteProfileService.clothingComplete(userId))
                .thenReturn(new ClothingProfileCompleteResponse(false));

        assertThatThrownBy(() -> gate.assertReadyFor(fashion(), userId))
                .isInstanceOf(ClothingProfileIncompleteException.class)
                .hasMessage("CLOTHING_PROFILE_INCOMPLETE");
    }

    @Test
    void fashionIntentWithCompleteProfilePassesThrough() {
        when(tasteProfileService.clothingComplete(userId))
                .thenReturn(new ClothingProfileCompleteResponse(true));

        assertThatCode(() -> gate.assertReadyFor(fashion(), userId)).doesNotThrowAnyException();
    }

    @Test
    void groceryIntentNeverGatesEvenWithIncompleteClothingProfile() {
        // service should not even be consulted for non-fashion intents
        assertThatCode(() -> gate.assertReadyFor(grocery(), userId)).doesNotThrowAnyException();
    }

    @Test
    void healthBeautyIntentNeverGates() {
        assertThatCode(
                        () ->
                                gate.assertReadyFor(
                                        new ParsedIntent(
                                                "shampoo",
                                                new BigDecimal("10"),
                                                ProductCategory.HEALTH_BEAUTY,
                                                List.of(),
                                                false,
                                                List.of()),
                                        userId))
                .doesNotThrowAnyException();
    }

    @Test
    void nullIntentPassesThrough() {
        assertThatCode(() -> gate.assertReadyFor(null, userId)).doesNotThrowAnyException();
    }

    private ParsedIntent fashion() {
        return new ParsedIntent(
                "summer dress under £60",
                new BigDecimal("60"),
                ProductCategory.FASHION,
                List.of(),
                false,
                List.of());
    }

    private ParsedIntent grocery() {
        return new ParsedIntent(
                "weekly groceries under £70",
                new BigDecimal("70"),
                ProductCategory.GROCERY,
                List.of(),
                false,
                List.of());
    }
}
