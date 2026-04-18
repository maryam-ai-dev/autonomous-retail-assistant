package com.aisleon.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeduplicationServiceTest {

    private final DeduplicationService dedup = new DeduplicationService();

    @Test
    void heinzBakedBeansAcrossRetailersMatch() {
        NormalizedProduct tesco = product(
                "t1",
                "heinz baked beans",
                Retailer.TESCO,
                ProductSubcategory.SNACKS,
                "415g");
        NormalizedProduct sainsburys = product(
                "s1",
                "heinz baked beans",
                Retailer.SAINSBURYS,
                ProductSubcategory.SNACKS,
                "415g");
        List<NormalizedProduct> annotated =
                dedup.annotateCrossRetailerMatches(List.of(tesco, sainsburys));
        assertThat(annotated.get(0).crossRetailerProductIds())
                .containsExactly("s1");
        assertThat(annotated.get(1).crossRetailerProductIds())
                .containsExactly("t1");
    }

    @Test
    void differentSizesDoNotMatch() {
        NormalizedProduct tesco = product(
                "t1",
                "heinz baked beans",
                Retailer.TESCO,
                ProductSubcategory.SNACKS,
                "415g");
        NormalizedProduct sainsburys = product(
                "s1",
                "heinz baked beans",
                Retailer.SAINSBURYS,
                ProductSubcategory.SNACKS,
                "200g");
        List<NormalizedProduct> annotated =
                dedup.annotateCrossRetailerMatches(List.of(tesco, sainsburys));
        assertThat(annotated.get(0).crossRetailerProductIds()).isEmpty();
        assertThat(annotated.get(1).crossRetailerProductIds()).isEmpty();
    }

    @Test
    void sameRetailerDoesNotCrossMatch() {
        NormalizedProduct a = product(
                "a",
                "heinz baked beans",
                Retailer.TESCO,
                ProductSubcategory.SNACKS,
                "415g");
        NormalizedProduct b = product(
                "b",
                "heinz baked beans",
                Retailer.TESCO,
                ProductSubcategory.SNACKS,
                "415g");
        List<NormalizedProduct> annotated =
                dedup.annotateCrossRetailerMatches(List.of(a, b));
        assertThat(annotated.get(0).crossRetailerProductIds()).isEmpty();
        assertThat(annotated.get(1).crossRetailerProductIds()).isEmpty();
    }

    @Test
    void sizeWithinTwentyPercentMatches() {
        // 500 vs 700: diff = 200/700 ≈ 28.6% → over tolerance
        assertThat(DeduplicationService.similarSize("500g", "700g")).isFalse();
        // 500 vs 550: diff = 50/550 ≈ 9% → within tolerance
        assertThat(DeduplicationService.similarSize("500g", "550g")).isTrue();
        // Different units never match
        assertThat(DeduplicationService.similarSize("500g", "500ml")).isFalse();
    }

    private static NormalizedProduct product(
            String id,
            String canonical,
            Retailer retailer,
            ProductSubcategory sub,
            String size) {
        return new NormalizedProduct(
                id,
                canonical,
                canonical + " " + size,
                "Heinz",
                retailer,
                ProductCategory.GROCERY,
                sub,
                new BigDecimal("1.25"),
                null,
                null,
                size,
                null,
                null,
                true,
                true,
                List.of(),
                List.of(),
                List.of(),
                0.9,
                Instant.now(),
                List.of(),
                List.of());
    }
}
