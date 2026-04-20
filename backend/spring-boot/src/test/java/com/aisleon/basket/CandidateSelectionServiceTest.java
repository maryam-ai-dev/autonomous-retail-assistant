package com.aisleon.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aisleon.catalogue.CertificationTag;
import com.aisleon.catalogue.DietaryTag;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.OfferFlag;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.Retailer;
import com.aisleon.scraping.CatalogueService;
import com.aisleon.scraping.CircuitState;
import com.aisleon.scraping.ConnectorRegistry;
import com.aisleon.scraping.ConnectorStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CandidateSelectionServiceTest {

    private final CatalogueService catalogueService = mock(CatalogueService.class);
    private final ConnectorRegistry registry = mock(ConnectorRegistry.class);
    private final BasketConstraintEngine engine = new BasketConstraintEngine();
    private final CandidateSelectionService service =
            new CandidateSelectionService(catalogueService, registry, engine);

    @Test
    void groceryProductsAreExcludedFromCandidatePool() {
        // B12.1: GROCERY is out of scope. Any grocery product returned by a
        // connector is filtered out of the candidate pool before dietary /
        // size / retailer filters run.
        when(registry.availableRetailers()).thenReturn(Set.of(Retailer.BOOTS));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(
                        Map.of(
                                Retailer.BOOTS,
                                List.of(
                                        product(
                                                "groc",
                                                "chicken",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.MEAT_POULTRY,
                                                Retailer.BOOTS,
                                                List.of(DietaryTag.HALAL_UNKNOWN),
                                                0.8),
                                        product(
                                                "hb",
                                                "shampoo",
                                                ProductCategory.HEALTH_BEAUTY,
                                                ProductSubcategory.HAIRCARE,
                                                Retailer.BOOTS,
                                                List.of(),
                                                0.9))));

        CandidatePool pool = service.select(healthBeautyIntent(), TasteProfile.empty(), null, 20);
        assertThat(pool.products())
                .extracting(NormalizedProduct::externalId)
                .doesNotContain("groc")
                .contains("hb");
    }

    @Test
    void allRetailersDownThrowsWithFailureReasons() {
        when(registry.availableRetailers()).thenReturn(Set.of());
        ConnectorStatus bootsDown =
                new ConnectorStatus(
                        Retailer.BOOTS,
                        false,
                        false,
                        CircuitState.OPEN,
                        null,
                        Instant.now(),
                        "timeout",
                        0,
                        0,
                        false);
        ConnectorStatus argosDown =
                new ConnectorStatus(
                        Retailer.ARGOS,
                        false,
                        false,
                        CircuitState.CLOSED,
                        null,
                        Instant.now(),
                        "bot detection",
                        0,
                        0,
                        false);
        when(registry.allStatuses()).thenReturn(List.of(bootsDown, argosDown));

        assertThatThrownBy(
                        () ->
                                service.select(
                                        healthBeautyIntent(), TasteProfile.empty(), null, 20))
                .isInstanceOfSatisfying(
                        NoRetailersAvailableException.class,
                        ex -> {
                            assertThat(ex.failureReasons())
                                    .containsEntry(Retailer.BOOTS, "timeout")
                                    .containsEntry(Retailer.ARGOS, "bot detection");
                        });
    }

    @Test
    void partialFailureContinuesWithRemainingRetailers() {
        when(registry.availableRetailers())
                .thenReturn(Set.of(Retailer.BOOTS, Retailer.ARGOS));
        when(registry.statusFor(Retailer.ARGOS))
                .thenReturn(
                        Optional.of(
                                new ConnectorStatus(
                                        Retailer.ARGOS,
                                        true,
                                        false,
                                        CircuitState.CLOSED,
                                        null,
                                        Instant.now(),
                                        "scraper error",
                                        0,
                                        0,
                                        false)));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(
                        Map.of(
                                Retailer.BOOTS,
                                List.of(
                                        product(
                                                "t",
                                                "moisturiser",
                                                ProductCategory.HEALTH_BEAUTY,
                                                ProductSubcategory.SKINCARE,
                                                Retailer.BOOTS,
                                                List.of(),
                                                0.9)),
                                Retailer.ARGOS,
                                List.of()));

        CandidatePool pool = service.select(healthBeautyIntent(), TasteProfile.empty(), null, 20);

        assertThat(pool.products()).hasSize(1);
        assertThat(pool.retailerFailures()).containsKey(Retailer.ARGOS);
    }

    @Test
    void preferredRetailerItemsRankedFirstWithOthersStillIncluded() {
        when(registry.availableRetailers())
                .thenReturn(Set.of(Retailer.BOOTS, Retailer.ARGOS));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(
                        Map.of(
                                Retailer.BOOTS,
                                List.of(
                                        product(
                                                "b-low",
                                                "moisturiser",
                                                ProductCategory.HEALTH_BEAUTY,
                                                ProductSubcategory.SKINCARE,
                                                Retailer.BOOTS,
                                                List.of(),
                                                0.4)),
                                Retailer.ARGOS,
                                List.of(
                                        product(
                                                "a-high",
                                                "moisturiser",
                                                ProductCategory.HEALTH_BEAUTY,
                                                ProductSubcategory.SKINCARE,
                                                Retailer.ARGOS,
                                                List.of(),
                                                0.9))));

        TasteProfile preferBoots =
                new TasteProfile(
                        false, false, false, List.of(Retailer.BOOTS), List.of(), List.of());
        CandidatePool pool = service.select(healthBeautyIntent(), preferBoots, null, 20);

        assertThat(pool.products())
                .extracting(NormalizedProduct::externalId)
                .containsExactly("b-low", "a-high");
    }

    @Test
    void cappedAtFiftyPerCategory() {
        List<NormalizedProduct> many = new java.util.ArrayList<>();
        for (int i = 0; i < 120; i++) {
            many.add(
                    product(
                            "id-" + i,
                            "item " + i,
                            ProductCategory.HEALTH_BEAUTY,
                            ProductSubcategory.SKINCARE,
                            Retailer.BOOTS,
                            List.of(),
                            0.5));
        }
        when(registry.availableRetailers()).thenReturn(Set.of(Retailer.BOOTS));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(Map.of(Retailer.BOOTS, many));

        CandidatePool pool = service.select(healthBeautyIntent(), TasteProfile.empty(), null, 200);
        assertThat(pool.products()).hasSize(50);
    }

    private static ParsedIntent healthBeautyIntent() {
        return new ParsedIntent(
                "skincare essentials",
                new BigDecimal("50"),
                ProductCategory.HEALTH_BEAUTY,
                List.of(),
                false,
                List.of());
    }

    private static NormalizedProduct product(
            String id,
            String name,
            ProductCategory category,
            ProductSubcategory subcategory,
            Retailer retailer,
            List<DietaryTag> dietary,
            double confidence) {
        return new NormalizedProduct(
                id,
                name,
                name,
                "brand",
                retailer,
                category,
                subcategory,
                new BigDecimal("2.00"),
                new BigDecimal("2.00"),
                "each",
                null,
                "https://example.com/img",
                "https://example.com/p/" + id,
                true,
                true,
                dietary,
                List.<CertificationTag>of(),
                List.<OfferFlag>of(),
                confidence,
                Instant.now(),
                List.of(),
                List.of());
    }
}
