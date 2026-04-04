package com.aisleon.discovery.application;

import com.aisleon.common.events.ProductCandidatesRankedEvent;
import com.aisleon.discovery.domain.DiscoveryResult;
import com.aisleon.discovery.domain.NormalizedProduct;
import com.aisleon.discovery.infrastructure.connectors.api.base.ApiConnectorResult;
import com.aisleon.discovery.infrastructure.connectors.api.ebay.EbayApiConnector;
import com.aisleon.discovery.infrastructure.connectors.browser.base.BrowserConnectorResult;
import com.aisleon.discovery.infrastructure.connectors.browser.playwright.PlaywrightBrowserConnector;
import com.aisleon.discovery.infrastructure.normalization.ProductNormalizationService;
import com.aisleon.preferences.domain.RetailPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConnectorSelectionService {

    private static final Logger log = LoggerFactory.getLogger(ConnectorSelectionService.class);
    private static final int MIN_API_RESULTS = 3;

    private final EbayApiConnector ebayApiConnector;
    private final PlaywrightBrowserConnector browserConnector;
    private final ProductNormalizationService normalizationService;
    private final PythonAiServiceClient pythonAiServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    public ConnectorSelectionService(EbayApiConnector ebayApiConnector,
                                     PlaywrightBrowserConnector browserConnector,
                                     ProductNormalizationService normalizationService,
                                     PythonAiServiceClient pythonAiServiceClient,
                                     ApplicationEventPublisher eventPublisher) {
        this.ebayApiConnector = ebayApiConnector;
        this.browserConnector = browserConnector;
        this.normalizationService = normalizationService;
        this.pythonAiServiceClient = pythonAiServiceClient;
        this.eventPublisher = eventPublisher;
    }

    public DiscoveryResult discover(String query, RetailPreferences preferences) {
        List<NormalizedProduct> allProducts = new ArrayList<>();
        List<String> sourcesUsed = new ArrayList<>();

        ApiConnectorResult ebayResult = ebayApiConnector.search(query, preferences);

        if (ebayResult.success()) {
            allProducts.addAll(ebayResult.products());
            sourcesUsed.add(ebayResult.sourceName());
        } else {
            log.warn("eBay connector failed: {}", ebayResult.errorMessage());
        }

        if (!ebayResult.success() || ebayResult.products().size() < MIN_API_RESULTS) {
            log.info("eBay returned {} results (min {}) — triggering browser fallback",
                    ebayResult.products().size(), MIN_API_RESULTS);

            BrowserConnectorResult browserResult = browserConnector.search(query, preferences);

            if (browserResult.success()) {
                List<NormalizedProduct> deduplicated = deduplicateProducts(
                        allProducts, browserResult.products());
                allProducts.addAll(deduplicated);
                sourcesUsed.add(browserResult.sourceName());
                log.info("Browser fallback added {} products (after dedup)", deduplicated.size());
            } else {
                log.warn("Browser fallback also failed: {}", browserResult.errorMessage());
            }
        }

        List<NormalizedProduct> normalized = normalizationService.normalize(allProducts);

        // Call Python AI service for ranking
        RankedDiscoveryResult ranked = pythonAiServiceClient.rank(query, normalized, preferences);

        // Publish ProductCandidatesRankedEvent
        List<String> topTitles = List.of();
        if (ranked.getRankedProducts() != null) {
            topTitles = ranked.getRankedProducts().stream()
                    .limit(5)
                    .map(rp -> {
                        Map<String, Object> product = rp.getProduct();
                        return product != null ? String.valueOf(product.getOrDefault("title", "")) : "";
                    })
                    .toList();
        }

        eventPublisher.publishEvent(new ProductCandidatesRankedEvent(
                preferences.getUserId(),
                query,
                topTitles,
                ranked.getConfidence(),
                ranked.getStrategyUsed(),
                sourcesUsed
        ));

        return new DiscoveryResult(
                normalized,
                sourcesUsed,
                normalized.size(),
                Instant.now(),
                ranked.getConfidence(),
                ranked.getStrategyUsed(),
                ranked.getUncertainty(),
                ranked.getRankedProducts()
        );
    }

    /**
     * Filters browser products that duplicate existing products by externalProductId
     * or by title similarity (case-insensitive containment).
     */
    private List<NormalizedProduct> deduplicateProducts(
            List<NormalizedProduct> existing,
            List<NormalizedProduct> incoming) {

        Set<String> existingIds = new HashSet<>();
        Set<String> existingTitlesLower = new HashSet<>();

        for (NormalizedProduct p : existing) {
            if (p.externalProductId() != null) {
                existingIds.add(p.externalProductId());
            }
            existingTitlesLower.add(p.title().toLowerCase());
        }

        List<NormalizedProduct> unique = new ArrayList<>();
        for (NormalizedProduct p : incoming) {
            if (p.externalProductId() != null && existingIds.contains(p.externalProductId())) {
                continue;
            }
            String titleLower = p.title().toLowerCase();
            boolean titleDuplicate = existingTitlesLower.stream()
                    .anyMatch(t -> t.contains(titleLower) || titleLower.contains(t));
            if (!titleDuplicate) {
                unique.add(p);
            }
        }
        return unique;
    }
}
