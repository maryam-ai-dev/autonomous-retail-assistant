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

/** Sprint B12.3 — size-filter behaviour across subcategories and preferences. */
class ApplySizeFilterTest {

    private final BasketConstraintEngine engine = new BasketConstraintEngine();

    @Test
    void profileWithNoSizesFilterIsNoop() {
        ClothingProfile empty =
                new ClothingProfile(null, null, null, null);
        List<NormalizedProduct> products = List.of(
                dress("d-s-only", "S"),
                trainers("t-6-only", "6"));
        assertThat(engine.applySizeFilter(products, empty))
                .hasSize(2);
    }

    @Test
    void footwearCandidatesWithoutDesiredShoeSizeAreExcluded() {
        ClothingProfile profile =
                new ClothingProfile(null, null, "6", null,
                        ClothingProfile.SizePreference.EXACT);
        List<NormalizedProduct> products = List.of(
                trainers("match", "5, 6, 7"),
                trainers("miss", "8, 9, 10"));
        List<NormalizedProduct> kept = engine.applySizeFilter(products, profile);
        assertThat(kept).extracting(NormalizedProduct::externalId).containsExactly("match");
    }

    @Test
    void productWithNullSizeTextAlwaysPassesThrough() {
        ClothingProfile profile =
                new ClothingProfile(null, null, "6", null,
                        ClothingProfile.SizePreference.EXACT);
        NormalizedProduct noSizeData = trainers("no-text", null);
        assertThat(engine.applySizeFilter(List.of(noSizeData), profile))
                .hasSize(1);
    }

    @Test
    void accessoriesSkipSizeFilterEvenForFashionCategory() {
        ClothingProfile profile =
                new ClothingProfile("S", null, null, null,
                        ClothingProfile.SizePreference.EXACT);
        NormalizedProduct bag = product(
                "bag",
                ProductCategory.FASHION,
                ProductSubcategory.ACCESSORIES,
                "one size");
        assertThat(engine.applySizeFilter(List.of(bag), profile))
                .hasSize(1);
    }

    @Test
    void nonFashionCategoryPassesThroughUntouched() {
        ClothingProfile profile =
                new ClothingProfile("S", null, null, null,
                        ClothingProfile.SizePreference.EXACT);
        NormalizedProduct shampoo = product(
                "sh",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.HAIRCARE,
                null);
        assertThat(engine.applySizeFilter(List.of(shampoo), profile)).hasSize(1);
    }

    @Test
    void sizeUpPreferenceIncludesOneLargerLetteredSize() {
        ClothingProfile profile =
                new ClothingProfile("M", null, null, null,
                        ClothingProfile.SizePreference.SIZE_UP);
        NormalizedProduct top = product(
                "t",
                ProductCategory.FASHION,
                ProductSubcategory.TOPS,
                "L, XL"); // M is absent; L is the "one size up"
        assertThat(engine.applySizeFilter(List.of(top), profile)).hasSize(1);
    }

    @Test
    void sizeDownPreferenceIncludesOneSmallerNumericSize() {
        ClothingProfile profile =
                new ClothingProfile(null, null, "7", null,
                        ClothingProfile.SizePreference.SIZE_DOWN);
        NormalizedProduct trainers = product(
                "sz-down",
                ProductCategory.FASHION,
                ProductSubcategory.FOOTWEAR,
                "5, 6");
        assertThat(engine.applySizeFilter(List.of(trainers), profile))
                .extracting(NormalizedProduct::externalId)
                .containsExactly("sz-down");
    }

    @Test
    void bottomsUseBottomsSizeField() {
        ClothingProfile profile =
                new ClothingProfile("XS", "L", null, null,
                        ClothingProfile.SizePreference.EXACT);
        NormalizedProduct trousers = product(
                "b",
                ProductCategory.FASHION,
                ProductSubcategory.BOTTOMS,
                "M, L");
        assertThat(engine.applySizeFilter(List.of(trousers), profile)).hasSize(1);
    }

    @Test
    void dressesUseDressSizeField() {
        ClothingProfile profile =
                new ClothingProfile("XS", null, null, "12",
                        ClothingProfile.SizePreference.EXACT);
        NormalizedProduct dress = product(
                "d",
                ProductCategory.FASHION,
                ProductSubcategory.DRESSES,
                "10, 12, 14");
        assertThat(engine.applySizeFilter(List.of(dress), profile)).hasSize(1);
    }

    private static NormalizedProduct dress(String id, String sizeText) {
        return product(id, ProductCategory.FASHION, ProductSubcategory.DRESSES, sizeText);
    }

    private static NormalizedProduct trainers(String id, String sizeText) {
        return product(id, ProductCategory.FASHION, ProductSubcategory.FOOTWEAR, sizeText);
    }

    private static NormalizedProduct product(
            String id, ProductCategory category, ProductSubcategory subcategory, String sizeText) {
        return new NormalizedProduct(
                id,
                "item",
                "item",
                "brand",
                Retailer.ASOS,
                category,
                subcategory,
                new BigDecimal("10.00"),
                null,
                "each",
                sizeText,
                "https://example.com/img",
                "https://example.com/p/" + id,
                true,
                true,
                List.<DietaryTag>of(),
                List.<CertificationTag>of(),
                List.<OfferFlag>of(),
                0.8,
                Instant.now(),
                List.of(),
                List.of());
    }
}
