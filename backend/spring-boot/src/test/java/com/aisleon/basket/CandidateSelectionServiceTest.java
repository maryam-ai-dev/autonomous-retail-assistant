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
    void halalOnlyProfileExcludesUnknownMeat() {
        when(registry.availableRetailers()).thenReturn(Set.of(Retailer.TESCO));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(
                        Map.of(
                                Retailer.TESCO,
                                List.of(
                                        product(
                                                "u",
                                                "chicken",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.MEAT_POULTRY,
                                                Retailer.TESCO,
                                                List.of(DietaryTag.HALAL_UNKNOWN),
                                                0.8),
                                        product(
                                                "v",
                                                "chicken",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.MEAT_POULTRY,
                                                Retailer.TESCO,
                                                List.of(DietaryTag.HALAL_VERIFIED),
                                                0.9),
                                        product(
                                                "a",
                                                "apple",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.FRUIT_VEG,
                                                Retailer.TESCO,
                                                List.of(),
                                                0.7))));

        TasteProfile halalProfile =
                new TasteProfile(true, false, false, List.of(), List.of(), List.of());
        CandidatePool pool = service.select(groceryIntent(), halalProfile, null, 20);

        assertThat(pool.products())
                .extracting(NormalizedProduct::externalId)
                .doesNotContain("u")
                .contains("v", "a");
    }

    @Test
    void allRetailersDownThrowsWithFailureReasons() {
        when(registry.availableRetailers()).thenReturn(Set.of());
        ConnectorStatus tescoDown =
                new ConnectorStatus(
                        Retailer.TESCO,
                        false,
                        false,
                        CircuitState.OPEN,
                        null,
                        Instant.now(),
                        "timeout",
                        0,
                        0,
                        true);
        ConnectorStatus sainsDown =
                new ConnectorStatus(
                        Retailer.SAINSBURYS,
                        false,
                        false,
                        CircuitState.CLOSED,
                        null,
                        Instant.now(),
                        "bot detection",
                        0,
                        0,
                        false);
        when(registry.allStatuses()).thenReturn(List.of(tescoDown, sainsDown));

        assertThatThrownBy(
                        () ->
                                service.select(
                                        groceryIntent(), TasteProfile.empty(), null, 20))
                .isInstanceOfSatisfying(
                        NoRetailersAvailableException.class,
                        ex -> {
                            assertThat(ex.failureReasons())
                                    .containsEntry(Retailer.TESCO, "timeout")
                                    .containsEntry(Retailer.SAINSBURYS, "bot detection");
                        });
    }

    @Test
    void partialFailureContinuesWithRemainingRetailers() {
        when(registry.availableRetailers())
                .thenReturn(Set.of(Retailer.TESCO, Retailer.SAINSBURYS));
        when(registry.statusFor(Retailer.SAINSBURYS))
                .thenReturn(
                        Optional.of(
                                new ConnectorStatus(
                                        Retailer.SAINSBURYS,
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
                                Retailer.TESCO,
                                List.of(
                                        product(
                                                "t",
                                                "oat milk",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.DAIRY,
                                                Retailer.TESCO,
                                                List.of(),
                                                0.9)),
                                Retailer.SAINSBURYS,
                                List.of()));

        CandidatePool pool = service.select(groceryIntent(), TasteProfile.empty(), null, 20);

        assertThat(pool.products()).hasSize(1);
        assertThat(pool.retailerFailures()).containsKey(Retailer.SAINSBURYS);
    }

    @Test
    void preferredRetailerItemsRankedFirstWithOthersStillIncluded() {
        when(registry.availableRetailers())
                .thenReturn(Set.of(Retailer.TESCO, Retailer.SAINSBURYS));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(
                        Map.of(
                                Retailer.TESCO,
                                List.of(
                                        product(
                                                "t-low",
                                                "oat milk",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.DAIRY,
                                                Retailer.TESCO,
                                                List.of(),
                                                0.4)),
                                Retailer.SAINSBURYS,
                                List.of(
                                        product(
                                                "s-high",
                                                "oat milk",
                                                ProductCategory.GROCERY,
                                                ProductSubcategory.DAIRY,
                                                Retailer.SAINSBURYS,
                                                List.of(),
                                                0.9))));

        TasteProfile preferTesco =
                new TasteProfile(
                        false, false, false, List.of(Retailer.TESCO), List.of(), List.of());
        CandidatePool pool = service.select(groceryIntent(), preferTesco, null, 20);

        assertThat(pool.products())
                .extracting(NormalizedProduct::externalId)
                .containsExactly("t-low", "s-high");
    }

    @Test
    void cappedAtFiftyPerCategory() {
        List<NormalizedProduct> many = new java.util.ArrayList<>();
        for (int i = 0; i < 120; i++) {
            many.add(
                    product(
                            "id-" + i,
                            "item " + i,
                            ProductCategory.GROCERY,
                            ProductSubcategory.SNACKS,
                            Retailer.TESCO,
                            List.of(),
                            0.5));
        }
        when(registry.availableRetailers()).thenReturn(Set.of(Retailer.TESCO));
        when(catalogueService.searchAll(anyString(), any(), anyInt()))
                .thenReturn(Map.of(Retailer.TESCO, many));

        CandidatePool pool = service.select(groceryIntent(), TasteProfile.empty(), null, 200);
        assertThat(pool.products()).hasSize(50);
    }

    private static ParsedIntent groceryIntent() {
        return new ParsedIntent(
                "weekly groceries",
                new BigDecimal("70"),
                ProductCategory.GROCERY,
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
