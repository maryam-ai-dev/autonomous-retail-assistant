package com.aisleon.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aisleon.catalogue.DietaryTag;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BasketConstraintEngineTest {

    private final BasketConstraintEngine engine = new BasketConstraintEngine();

    @Test
    void budgetCapDetectsOverBudget() {
        BasketItem a = item("a", "snack", ProductSubcategory.SNACKS, new BigDecimal("45.00"), List.of());
        BasketItem b = item("b", "snack", ProductSubcategory.SNACKS, new BigDecimal("26.00"), List.of());
        assertThat(engine.exceedsBudget(List.of(a, b), new BigDecimal("70.00"))).isTrue();
    }

    @Test
    void dietaryFilterRemovesHalalUnknownMeatOnly() {
        NormalizedProduct chickenUnknown = product("p1", "chicken", ProductSubcategory.MEAT_POULTRY, List.of(DietaryTag.HALAL_UNKNOWN));
        NormalizedProduct chickenVerified = product("p2", "chicken", ProductSubcategory.MEAT_POULTRY, List.of(DietaryTag.HALAL_VERIFIED));
        NormalizedProduct apple = product("p3", "apple", ProductSubcategory.FRUIT_VEG, List.of());
        TasteProfile profile = new TasteProfile(true, false, false, List.of(), List.of(), List.of());

        List<NormalizedProduct> filtered = engine.applyDietaryFilter(
                List.of(chickenUnknown, chickenVerified, apple), profile);

        assertThat(filtered).containsExactly(chickenVerified, apple);
    }

    @Test
    void trimRemovesCheapestUntilWithinBudget() {
        BasketItem a = item("a", "a", ProductSubcategory.SNACKS, new BigDecimal("30.00"), List.of());
        BasketItem b = item("b", "b", ProductSubcategory.SNACKS, new BigDecimal("28.00"), List.of());
        BasketItem c = item("c", "c", ProductSubcategory.SNACKS, new BigDecimal("15.00"), List.of());

        List<BasketItem> trimmed =
                engine.trimToBudget(List.of(a, b, c), new BigDecimal("60.00"));

        assertThat(trimmed).extracting(BasketItem::id).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void emptyAfterTrimThrowsBudgetTooLowException() {
        BasketItem a = item("a", "a", ProductSubcategory.SNACKS, new BigDecimal("40.00"), List.of());
        assertThatThrownBy(() ->
                engine.trimToBudget(List.of(a), new BigDecimal("10.00")))
                .isInstanceOf(BudgetTooLowException.class);
    }

    @Test
    void retailerFilterHonoursDenyList() {
        NormalizedProduct tesco = product("p1", "x", ProductSubcategory.SNACKS, List.of());
        NormalizedProduct sainsburys = new NormalizedProduct(
                "p2", "x", "X", "b",
                Retailer.SAINSBURYS,
                ProductCategory.GROCERY, ProductSubcategory.SNACKS,
                new BigDecimal("1.00"), null, null, null, null, null,
                true, true, List.of(), List.of(), List.of(),
                0.9, Instant.now(), List.of(), List.of());
        TasteProfile deny = new TasteProfile(false, false, false, List.of(),
                List.of(Retailer.SAINSBURYS), List.of());
        List<NormalizedProduct> filtered = engine.applyRetailerFilter(
                List.of(tesco, sainsburys), deny);
        assertThat(filtered).containsExactly(tesco);
    }

    private static NormalizedProduct product(
            String id,
            String canonical,
            ProductSubcategory sub,
            List<DietaryTag> dietary) {
        return new NormalizedProduct(
                id, canonical, canonical, "brand",
                Retailer.TESCO,
                sub == ProductSubcategory.FRUIT_VEG
                        ? ProductCategory.GROCERY
                        : ProductCategory.GROCERY,
                sub,
                new BigDecimal("1.00"),
                null, null, null, null, null,
                true, true, dietary, List.of(), List.of(),
                0.9, Instant.now(), List.of(), List.of());
    }

    private static BasketItem item(
            String id,
            String canonical,
            ProductSubcategory sub,
            BigDecimal price,
            List<DietaryTag> dietary) {
        NormalizedProduct p = new NormalizedProduct(
                id, canonical, canonical, "brand",
                Retailer.TESCO, ProductCategory.GROCERY, sub,
                price, null, null, null, null, null,
                true, true, dietary, List.of(), List.of(),
                0.9, Instant.now(), List.of(), List.of());
        return new BasketItem(id, p, 1, Optional.empty());
    }
}
