package com.aisleon.basket;

import static org.assertj.core.api.Assertions.assertThat;

import com.aisleon.catalogue.CertificationTag;
import com.aisleon.catalogue.DietaryTag;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.OfferFlag;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Sprint B12.4 — halal_only filter targets HEALTH_BEAUTY, not fashion/electronics. */
class ApplyDietaryFilterTest {

    private final BasketConstraintEngine engine = new BasketConstraintEngine();

    @Test
    void halalOnlyExcludesHalalUnknownShampoo() {
        NormalizedProduct shampoo = product(
                "sh-unknown",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.HAIRCARE,
                List.of(DietaryTag.HALAL_UNKNOWN));
        assertThat(engine.applyDietaryFilter(List.of(shampoo), halalOnly()))
                .isEmpty();
    }

    @Test
    void halalOnlyKeepsHalalLikelyShampoo() {
        NormalizedProduct shampoo = product(
                "sh-likely",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.HAIRCARE,
                List.of(DietaryTag.HALAL_LIKELY));
        assertThat(engine.applyDietaryFilter(List.of(shampoo), halalOnly()))
                .hasSize(1);
    }

    @Test
    void halalOnlyExcludesHalalUnknownMakeupAndFragrance() {
        NormalizedProduct makeup = product(
                "mk",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.MAKEUP,
                List.of(DietaryTag.HALAL_UNKNOWN));
        NormalizedProduct fragrance = product(
                "fr",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.FRAGRANCE,
                List.of(DietaryTag.HALAL_UNKNOWN));
        assertThat(engine.applyDietaryFilter(List.of(makeup, fragrance), halalOnly()))
                .isEmpty();
    }

    @Test
    void halalOnlyKeepsFashionDressEvenWithoutHalalTag() {
        NormalizedProduct dress = product(
                "d",
                ProductCategory.FASHION,
                ProductSubcategory.DRESSES,
                List.of());
        assertThat(engine.applyDietaryFilter(List.of(dress), halalOnly()))
                .extracting(NormalizedProduct::externalId)
                .containsExactly("d");
    }

    @Test
    void halalOnlyKeepsElectronicsKettleEvenWithoutHalalTag() {
        NormalizedProduct kettle = product(
                "k",
                ProductCategory.GENERAL_MERCHANDISE,
                ProductSubcategory.KITCHEN,
                List.of());
        assertThat(engine.applyDietaryFilter(List.of(kettle), halalOnly()))
                .extracting(NormalizedProduct::externalId)
                .containsExactly("k");
    }

    private static TasteProfile halalOnly() {
        return new TasteProfile(true, false, false, List.of(), List.of(), List.of());
    }

    private static NormalizedProduct product(
            String id,
            ProductCategory category,
            ProductSubcategory subcategory,
            List<DietaryTag> dietary) {
        return new NormalizedProduct(
                id,
                "item",
                "item",
                "brand",
                Retailer.BOOTS,
                category,
                subcategory,
                new BigDecimal("10.00"),
                null,
                "each",
                null,
                "https://example.com/img",
                "https://example.com/p/" + id,
                true,
                true,
                dietary,
                List.<CertificationTag>of(),
                List.<OfferFlag>of(),
                0.8,
                Instant.now(),
                List.of(),
                List.of());
    }
}
