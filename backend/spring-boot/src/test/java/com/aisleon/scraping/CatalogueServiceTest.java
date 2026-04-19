package com.aisleon.scraping;

import static org.assertj.core.api.Assertions.assertThat;

import com.aisleon.catalogue.DietaryTaggingService;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductNormalizer;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogueServiceTest {

    private final ProductNormalizer normalizer =
            new ProductNormalizer(new DietaryTaggingService());
    private final ScraperResultValidator validator = new ScraperResultValidator();
    private final InMemoryCatalogueCache cache = new InMemoryCatalogueCache();
    private final StaleCacheCounter staleCounter = new StaleCacheCounter();
    private final ValidationFailureRecorder validationRecorder = new ValidationFailureRecorder();

    @Test
    void cacheMissCallsScraperThenCaches() {
        StubConnector connector = new StubConnector(Retailer.TESCO,
                List.of(raw("p1"), raw("p2"), raw("p3")));
        ConnectorRegistry registry =
                new ConnectorRegistry(
                        List.of(connector), "", staleCounter, validationRecorder);
        CatalogueService service =
                new CatalogueService(
                        registry, normalizer, validator, cache, staleCounter, validationRecorder);

        List<NormalizedProduct> first = service.search("milk", Retailer.TESCO, 10);
        assertThat(first).hasSize(3);
        assertThat(connector.callCount).isEqualTo(1);

        List<NormalizedProduct> second = service.search("milk", Retailer.TESCO, 10);
        assertThat(second).hasSize(3);
        assertThat(connector.callCount).as("second call served from cache").isEqualTo(1);
    }

    @Test
    void scraperReturningEmptyServesEmptyWhenNoCache() {
        StubConnector connector = new StubConnector(Retailer.TESCO, List.of());
        ConnectorRegistry registry =
                new ConnectorRegistry(
                        List.of(connector), "", staleCounter, validationRecorder);
        CatalogueService service =
                new CatalogueService(
                        registry, normalizer, validator, cache, staleCounter, validationRecorder);

        List<NormalizedProduct> result = service.search("xyz", Retailer.TESCO, 10);
        assertThat(result).isEmpty();
    }

    @Test
    void scrapeRejectedByValidatorDoesNotPoisonCache() {
        StubConnector connector = new StubConnector(Retailer.TESCO, List.of(raw("only-one")));
        ConnectorRegistry registry =
                new ConnectorRegistry(
                        List.of(connector), "", staleCounter, validationRecorder);
        CatalogueService service =
                new CatalogueService(
                        registry, normalizer, validator, cache, staleCounter, validationRecorder);

        List<NormalizedProduct> result = service.search("bread", Retailer.TESCO, 10);
        assertThat(result).isEmpty();
        assertThat(cache.get(Retailer.TESCO, "bread")).isEmpty();
    }

    @Test
    void scrapeRejectionUpdatesConnectorStatusLastFailureReason() {
        StubConnector connector = new StubConnector(Retailer.TESCO, List.of(raw("only-one")));
        ConnectorRegistry registry =
                new ConnectorRegistry(
                        List.of(connector), "", staleCounter, validationRecorder);
        CatalogueService service =
                new CatalogueService(
                        registry, normalizer, validator, cache, staleCounter, validationRecorder);

        service.search("bread", Retailer.TESCO, 10);

        ConnectorStatus status =
                registry.allStatuses().stream()
                        .filter(s -> s.retailer() == Retailer.TESCO)
                        .findFirst()
                        .orElseThrow();
        assertThat(status.lastFailureReason()).isEqualTo("REJECT_TOO_FEW");
    }

    private static RawScraperProduct raw(String id) {
        return new RawScraperProduct(
                id,
                "Sample product " + id,
                "Brand",
                ProductCategory.GROCERY,
                ProductSubcategory.SNACKS,
                new BigDecimal("1.99"),
                false,
                new BigDecimal("0.99"),
                "per 100g",
                "200g",
                "https://img.example/" + id + ".jpg",
                "https://shop.example/p/" + id,
                true,
                true,
                List.of(),
                List.of(),
                Instant.now());
    }

    private static class StubConnector implements RetailerConnector {
        private final Retailer retailer;
        private final List<RawScraperProduct> products;
        int callCount = 0;

        StubConnector(Retailer retailer, List<RawScraperProduct> products) {
            this.retailer = retailer;
            this.products = products;
        }

        @Override
        public Retailer getRetailer() {
            return retailer;
        }

        @Override
        public List<RawScraperProduct> search(String query, int maxResults) {
            callCount++;
            return products;
        }

        @Override
        public boolean isHealthy() {
            return true;
        }

        @Override
        public ConnectorStatus getStatus() {
            return new ConnectorStatus(
                    retailer, true, false, CircuitState.CLOSED,
                    Instant.now(), null, null, 0, 0, false);
        }
    }
}
