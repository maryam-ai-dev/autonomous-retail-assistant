package com.aisleon.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductNormalizerTest {

    private final ProductNormalizer normalizer =
            new ProductNormalizer(new DietaryTaggingService());

    @Test
    void stripsCommonTrackingParameters() {
        String url =
                "https://www.tesco.com/groceries/en-GB/products/123?utm_source=x&gclid=abc&ref=y";
        String cleaned = ProductNormalizer.stripTrackingParams(url);
        assertThat(cleaned).doesNotContain("utm_source");
        assertThat(cleaned).doesNotContain("gclid");
        assertThat(cleaned).doesNotContain("ref=");
    }

    @Test
    void preservesNonTrackingParameters() {
        String url = "https://example.com/item?sku=123&color=blue&utm_medium=email";
        String cleaned = ProductNormalizer.stripTrackingParams(url);
        assertThat(cleaned).contains("sku=123");
        assertThat(cleaned).contains("color=blue");
        assertThat(cleaned).doesNotContain("utm_");
    }

    @Test
    void twentyProductsNormaliseWithoutException() {
        for (int i = 0; i < 20; i++) {
            RawScraperProduct raw = sampleRaw(i);
            NormalizedProduct np = normalizer.normalize(raw, Retailer.TESCO);
            assertThat(np.confidenceScore()).isGreaterThan(0.0);
            assertThat(np.productUrl()).doesNotContain("utm_");
        }
    }

    @Test
    void halalLikelyAddsNormalizationWarning() {
        RawScraperProduct raw = new RawScraperProduct(
                "p1",
                "Fresh salmon fillet 200g",
                "Tesco",
                ProductCategory.GROCERY,
                ProductSubcategory.FISH_SEAFOOD,
                new BigDecimal("4.50"),
                false,
                new BigDecimal("2.25"),
                "per 100g",
                "200g",
                "https://img.example/salmon.jpg",
                "https://tesco.com/p/1",
                true,
                true,
                List.of(),
                List.of(),
                Instant.now());
        NormalizedProduct np = normalizer.normalize(raw, Retailer.TESCO);
        assertThat(np.dietaryTags()).contains(DietaryTag.HALAL_LIKELY);
        assertThat(np.normalizationWarnings()).isNotEmpty();
    }

    private static RawScraperProduct sampleRaw(int i) {
        return new RawScraperProduct(
                "p" + i,
                "Sample Product " + i + " 500g",
                "Brand" + i,
                ProductCategory.GROCERY,
                ProductSubcategory.SNACKS,
                BigDecimal.valueOf(1.0 + i * 0.1),
                false,
                BigDecimal.valueOf(2.0),
                "per 100g",
                "500g",
                "https://img.example/" + i + ".jpg",
                "https://shop.example/p/" + i + "?utm_source=foo",
                true,
                true,
                List.of(),
                List.of(),
                Instant.now());
    }
}
