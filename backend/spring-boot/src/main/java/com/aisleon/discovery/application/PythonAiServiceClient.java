package com.aisleon.discovery.application;

import com.aisleon.discovery.domain.NormalizedProduct;
import com.aisleon.preferences.domain.RetailPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PythonAiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PythonAiServiceClient.class);

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public PythonAiServiceClient(
            @Value("${python-ai-service.url:http://localhost:8001}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calls the Python ranking service. On failure, returns a fallback sorted by price.
     */
    public RankedDiscoveryResult rank(
            String query,
            List<NormalizedProduct> products,
            RetailPreferences preferences) {
        try {
            Map<String, Object> requestBody = buildRequest(query, products, preferences);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<RankedDiscoveryResult> response = restTemplate.postForEntity(
                    baseUrl + "/ranking/rank", request, RankedDiscoveryResult.class);

            RankedDiscoveryResult result = response.getBody();
            if (result == null) {
                log.warn("Python AI service returned null response — using fallback");
                return fallback(products);
            }
            return result;

        } catch (Exception e) {
            log.warn("Python AI service call failed: {} — using price-sorted fallback", e.getMessage());
            return fallback(products);
        }
    }

    private Map<String, Object> buildRequest(
            String query,
            List<NormalizedProduct> products,
            RetailPreferences prefs) {

        Map<String, Object> userPrefs = new HashMap<>();
        userPrefs.put("budget_cap", prefs.getBudgetCap());
        userPrefs.put("preferred_brands", prefs.getPreferredBrands());
        userPrefs.put("blocked_brands", prefs.getBlockedBrands());
        userPrefs.put("blocked_categories", prefs.getBlockedCategories());
        userPrefs.put("allow_substitutions", prefs.getAllowSubstitutions());
        userPrefs.put("max_substitution_price_delta", prefs.getMaxSubstitutionPriceDelta());
        userPrefs.put("approval_threshold", prefs.getApprovalThreshold());

        List<Map<String, Object>> productMaps = products.stream()
                .map(this::productToMap)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("products", productMaps);
        body.put("user_preferences", userPrefs);
        return body;
    }

    private Map<String, Object> productToMap(NormalizedProduct p) {
        Map<String, Object> m = new HashMap<>();
        m.put("source_type", p.sourceType() != null ? p.sourceType().name() : "API");
        m.put("source_name", p.sourceName());
        m.put("external_product_id", p.externalProductId());
        m.put("title", p.title());
        m.put("description", p.description());
        m.put("category", p.category());
        m.put("brand", p.brand());
        m.put("price", p.price());
        m.put("currency", p.currency());
        m.put("availability", p.availability());
        m.put("merchant_id", p.merchantId() != null ? p.merchantId().toString() : null);
        m.put("merchant_name", p.merchantName());
        m.put("merchant_rating", p.merchantRating());
        m.put("shipping_cost", p.shippingCost());
        m.put("shipping_eta", p.shippingEta());
        m.put("image_urls", p.imageUrls());
        m.put("product_url", p.productUrl());
        m.put("attributes", p.attributes());
        return m;
    }

    private RankedDiscoveryResult fallback(List<NormalizedProduct> products) {
        List<NormalizedProduct> sorted = products.stream()
                .sorted(Comparator.comparing(NormalizedProduct::price,
                        Comparator.nullsLast(BigDecimal::compareTo)))
                .toList();

        List<RankedDiscoveryResult.RankedProductDto> ranked = new java.util.ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            NormalizedProduct p = sorted.get(i);
            ranked.add(RankedDiscoveryResult.RankedProductDto.builder()
                    .product(productToMap(p))
                    .score(0.0)
                    .rank(i + 1)
                    .explanation(Map.of("top_reasons", List.of("Sorted by price (fallback)")))
                    .trustScore(Map.of("overall_trust_score", 0.0))
                    .build());
        }

        return RankedDiscoveryResult.builder()
                .rankedProducts(ranked)
                .strategyUsed("fallback_price_sort")
                .confidence(0.0)
                .uncertainty(RankedDiscoveryResult.UncertaintyDto.builder()
                        .confidence(0.0)
                        .isUncertain(true)
                        .reasons(List.of("Python AI service unavailable — using price fallback"))
                        .recommendation("ask_user")
                        .build())
                .filteredCount(0)
                .sourcesUsed(List.of("fallback"))
                .build();
    }
}
