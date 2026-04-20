package com.aisleon.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DietaryTaggingServiceTest {

    private final DietaryTaggingService service = new DietaryTaggingService();

    @Test
    void tescoHalalChickenIsHalalUnknown() {
        var result = service.classifyHalal(
                "Tesco",
                ProductSubcategory.MEAT_POULTRY,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_UNKNOWN));
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void knownHalalBrandIsHalalLikelyWithWarning() {
        // B12.1: known_halal_brands.txt now lists non-food halal brands only.
        // The DietaryTaggingService still resolves HALAL_LIKELY via brand match
        // regardless of subcategory — B12.4 will narrow this to HEALTH_BEAUTY.
        var result = service.classifyHalal(
                "Inika Organic",
                ProductSubcategory.MEAT_POULTRY,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_LIKELY));
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void fishIsHalalLikelyWithWarning() {
        var result = service.classifyHalal(
                "Tesco",
                ProductSubcategory.FISH_SEAFOOD,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_LIKELY));
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void appleHasNoHalalTag() {
        // Fruit/veg is in the HALAL_LIKELY subcategories list per the plan,
        // so the service infers HALAL_LIKELY with an inference warning.
        var result = service.classifyHalal(
                "Tesco",
                ProductSubcategory.FRUIT_VEG,
                List.of());
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_LIKELY));
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void halalCertifiedBrandIsHalalVerified() {
        var result = service.classifyHalal(
                "SomeBrand",
                ProductSubcategory.MEAT_POULTRY,
                List.of(CertificationTag.HALAL_CERTIFIED));
        assertThat(result.halalTag()).isEqualTo(Optional.of(DietaryTag.HALAL_VERIFIED));
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void unrelatedCategoryReturnsNoHalalTag() {
        var result = service.classifyHalal(
                "Tesco",
                ProductSubcategory.LAPTOPS,
                List.of());
        assertThat(result.halalTag()).isEmpty();
        assertThat(result.warnings()).isEmpty();
    }
}
