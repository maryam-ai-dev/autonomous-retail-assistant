package com.aisleon.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Sprint B12.4 — halal classification for non-food. Only HEALTH_BEAUTY
 * subcategories plus VITAMINS_SUPPLEMENTS attract halal tags. FASHION,
 * ELECTRONICS and GENERAL_MERCHANDISE never carry a halal tag.
 */
class DietaryTaggingServiceTest {

    private final DietaryTaggingService service = new DietaryTaggingService();

    @Test
    void unknownShampooIsHalalUnknownWithNonFoodWarning() {
        var result = service.classifyHalal(
                "RandomBrand",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.HAIRCARE,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_UNKNOWN));
        assertThat(result.warnings())
                .anyMatch(w -> w.toLowerCase().contains("alcohol")
                        || w.toLowerCase().contains("porcine"));
    }

    @Test
    void knownHalalBrandShampooIsHalalLikelyWithBrandWarning() {
        var result = service.classifyHalal(
                "Inika Organic",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.HAIRCARE,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_LIKELY));
        assertThat(result.warnings())
                .anyMatch(w -> w.toLowerCase().contains("brand"));
    }

    @Test
    void halalCertifiedFragranceIsHalalVerified() {
        var result = service.classifyHalal(
                "SomeHouse",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.FRAGRANCE,
                List.of(CertificationTag.HALAL_CERTIFIED));
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_VERIFIED));
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void fashionDressHasNoHalalTag() {
        var result = service.classifyHalal(
                "ASOS DESIGN",
                ProductCategory.FASHION,
                ProductSubcategory.DRESSES,
                List.of());
        assertThat(result.halalTag()).isEmpty();
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void electronicsHasNoHalalTag() {
        var result = service.classifyHalal(
                "Samsung",
                ProductCategory.ELECTRONICS,
                ProductSubcategory.PHONES,
                List.of());
        assertThat(result.halalTag()).isEmpty();
    }

    @Test
    void generalMerchandiseHasNoHalalTag() {
        var result = service.classifyHalal(
                "Russell Hobbs",
                ProductCategory.GENERAL_MERCHANDISE,
                ProductSubcategory.KITCHEN,
                List.of());
        assertThat(result.halalTag()).isEmpty();
    }

    @Test
    void vitaminsSupplementsInHealthBeautyScopeIsHalalUnknown() {
        var result = service.classifyHalal(
                "GenericVitamins",
                ProductCategory.HEALTH_BEAUTY,
                ProductSubcategory.VITAMINS_SUPPLEMENTS,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_UNKNOWN));
    }

    @Test
    void halalCertifiedFashionStillReturnsNoTagBecauseCategoryGatesFirst() {
        // Category gate runs before certification check — a "halal certified"
        // cotton dress still returns no halal tag because halal is not
        // applicable to FASHION products.
        var result = service.classifyHalal(
                "SomeBrand",
                ProductCategory.FASHION,
                ProductSubcategory.DRESSES,
                List.of(CertificationTag.HALAL_CERTIFIED));
        assertThat(result.halalTag()).isEmpty();
    }
}
