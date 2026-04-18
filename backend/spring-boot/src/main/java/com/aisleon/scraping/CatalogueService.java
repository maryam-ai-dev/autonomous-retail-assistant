package com.aisleon.scraping;

import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductNormalizer;
import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CatalogueService {

    private static final Logger log = LoggerFactory.getLogger(CatalogueService.class);
    private static final Duration FRESH_TTL = Duration.ofHours(6);
    private static final Duration STALE_TTL = Duration.ofHours(24);
    private static final String STALE_WARNING = "served from stale cache — scraper unavailable";

    private final ConnectorRegistry registry;
    private final ProductNormalizer normalizer;
    private final ScraperResultValidator validator;
    private final CatalogueCache cache;

    public CatalogueService(
            ConnectorRegistry registry,
            ProductNormalizer normalizer,
            ScraperResultValidator validator,
            CatalogueCache cache) {
        this.registry = registry;
        this.normalizer = normalizer;
        this.validator = validator;
        this.cache = cache;
    }

    public List<NormalizedProduct> search(String query, Retailer retailer, int maxResults) {
        Optional<CatalogueCache.Entry> cached = cache.get(retailer, query);
        if (cached.isPresent() && isFresh(cached.get())) {
            return cached.get().products();
        }

        RetailerConnector connector;
        try {
            connector = registry.getConnector(retailer);
        } catch (ConnectorUnavailableException e) {
            return serveStaleOrEmpty(cached, retailer, "connector unavailable");
        }

        List<RawScraperProduct> raw;
        try {
            raw = connector.search(query, maxResults);
        } catch (RuntimeException e) {
            log.warn("Scraper error for {}: {}", retailer, e.getMessage());
            return serveStaleOrEmpty(cached, retailer, "scraper error");
        }

        if (raw == null || raw.isEmpty()) {
            return serveStaleOrEmpty(cached, retailer, "scraper returned empty");
        }

        List<NormalizedProduct> normalized = raw.stream()
                .map(r -> normalizer.normalize(r, retailer))
                .toList();

        ScraperResultValidator.Outcome outcome = validator.validate(normalized);
        if (outcome != ScraperResultValidator.Outcome.ACCEPT) {
            log.warn("Scrape rejected ({} for {}): {}", outcome, retailer,
                    normalized.size());
            return serveStaleOrEmpty(cached, retailer, outcome.name());
        }

        cache.put(retailer, query, normalized);
        return normalized;
    }

    public Map<Retailer, List<NormalizedProduct>> searchAll(
            String query, Set<Retailer> retailers, int maxResultsPerRetailer) {
        Map<Retailer, List<NormalizedProduct>> out = new HashMap<>();
        for (Retailer r : retailers) {
            out.put(r, search(query, r, maxResultsPerRetailer));
        }
        return out;
    }

    private boolean isFresh(CatalogueCache.Entry entry) {
        return Duration.between(entry.storedAt(), Instant.now()).compareTo(FRESH_TTL) <= 0;
    }

    private boolean withinStaleWindow(CatalogueCache.Entry entry) {
        return Duration.between(entry.storedAt(), Instant.now()).compareTo(STALE_TTL) <= 0;
    }

    private List<NormalizedProduct> serveStaleOrEmpty(
            Optional<CatalogueCache.Entry> cached, Retailer retailer, String reason) {
        if (cached.isPresent() && withinStaleWindow(cached.get())) {
            log.info("Serving stale cache for {} (reason={})", retailer, reason);
            return appendStaleWarning(cached.get().products());
        }
        return List.of();
    }

    private List<NormalizedProduct> appendStaleWarning(List<NormalizedProduct> products) {
        List<NormalizedProduct> out = new ArrayList<>(products.size());
        for (NormalizedProduct p : products) {
            List<String> warnings = new ArrayList<>(p.normalizationWarnings());
            if (!warnings.contains(STALE_WARNING)) warnings.add(STALE_WARNING);
            out.add(new NormalizedProduct(
                    p.externalId(),
                    p.canonicalName(),
                    p.displayName(),
                    p.brand(),
                    p.retailer(),
                    p.category(),
                    p.subcategory(),
                    p.price(),
                    p.unitPrice(),
                    p.unitBasis(),
                    p.sizeText(),
                    p.imageUrl(),
                    p.productUrl(),
                    p.isAvailable(),
                    p.isBasketable(),
                    p.dietaryTags(),
                    p.certificationTags(),
                    p.offerFlags(),
                    p.confidenceScore(),
                    p.sourceFetchedAt(),
                    List.copyOf(warnings),
                    p.crossRetailerProductIds()));
        }
        return out;
    }
}
