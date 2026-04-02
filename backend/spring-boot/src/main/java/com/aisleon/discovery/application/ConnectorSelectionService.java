package com.aisleon.discovery.application;

import com.aisleon.discovery.domain.DiscoveryResult;
import com.aisleon.discovery.domain.NormalizedProduct;
import com.aisleon.discovery.infrastructure.connectors.api.base.ApiConnectorResult;
import com.aisleon.discovery.infrastructure.connectors.api.ebay.EbayApiConnector;
import com.aisleon.discovery.infrastructure.normalization.ProductNormalizationService;
import com.aisleon.preferences.domain.RetailPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectorSelectionService {

    private static final Logger log = LoggerFactory.getLogger(ConnectorSelectionService.class);
    private static final int MIN_API_RESULTS = 3;

    private final EbayApiConnector ebayApiConnector;
    private final ProductNormalizationService normalizationService;

    public ConnectorSelectionService(EbayApiConnector ebayApiConnector,
                                     ProductNormalizationService normalizationService) {
        this.ebayApiConnector = ebayApiConnector;
        this.normalizationService = normalizationService;
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
            log.warn("eBay returned fewer than {} results — browser fallback is a stub (no-op)",
                    MIN_API_RESULTS);
            // Browser fallback stub — will be replaced in Sprint 16
        }

        List<NormalizedProduct> normalized = normalizationService.normalize(allProducts);

        return new DiscoveryResult(
                normalized,
                sourcesUsed,
                normalized.size(),
                Instant.now()
        );
    }
}
