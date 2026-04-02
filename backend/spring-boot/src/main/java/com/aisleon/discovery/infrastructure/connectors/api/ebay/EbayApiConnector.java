package com.aisleon.discovery.infrastructure.connectors.api.ebay;

import com.aisleon.discovery.domain.NormalizedProduct;
import com.aisleon.discovery.infrastructure.connectors.api.base.ApiConnector;
import com.aisleon.discovery.infrastructure.connectors.api.base.ApiConnectorResult;
import com.aisleon.merchant.domain.SourceType;
import com.aisleon.merchant.infrastructure.MerchantJpaEntity;
import com.aisleon.merchant.infrastructure.MerchantRepository;
import com.aisleon.preferences.domain.RetailPreferences;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class EbayApiConnector implements ApiConnector {

    private static final String SEARCH_URL =
            "https://api.ebay.com/buy/browse/v1/item_summary/search";
    private static final String SOURCE_NAME = "ebay";

    private final EbayAuthClient authClient;
    private final MerchantRepository merchantRepository;
    private final RestTemplate restTemplate;

    public EbayApiConnector(EbayAuthClient authClient, MerchantRepository merchantRepository) {
        this.authClient = authClient;
        this.merchantRepository = merchantRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApiConnectorResult search(String query, RetailPreferences preferences) {
        try {
            String token = authClient.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            String url = SEARCH_URL + "?q=" + query + "&limit=20";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("itemSummaries")) {
                return new ApiConnectorResult(List.of(), SOURCE_NAME, true, null);
            }

            UUID merchantId = lookupMerchantId();
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("itemSummaries");
            List<NormalizedProduct> products = new ArrayList<>();

            for (Map<String, Object> item : items) {
                products.add(mapToNormalizedProduct(item, merchantId));
            }

            return new ApiConnectorResult(products, SOURCE_NAME, true, null);

        } catch (Exception e) {
            return new ApiConnectorResult(List.of(), SOURCE_NAME, false, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private NormalizedProduct mapToNormalizedProduct(Map<String, Object> item, UUID merchantId) {
        String title = (String) item.getOrDefault("title", "");
        String itemId = (String) item.getOrDefault("itemId", "");

        BigDecimal price = BigDecimal.ZERO;
        String currency = "USD";
        Map<String, Object> priceMap = (Map<String, Object>) item.get("price");
        if (priceMap != null) {
            price = new BigDecimal(priceMap.getOrDefault("value", "0").toString());
            currency = (String) priceMap.getOrDefault("currency", "USD");
        }

        String sellerName = "";
        Double merchantRating = null;
        Map<String, Object> seller = (Map<String, Object>) item.get("seller");
        if (seller != null) {
            sellerName = (String) seller.getOrDefault("username", "");
            merchantRating = extractMerchantRating(seller);
        }

        List<String> imageUrls = List.of();
        Map<String, Object> image = (Map<String, Object>) item.get("image");
        if (image != null && image.containsKey("imageUrl")) {
            imageUrls = List.of((String) image.get("imageUrl"));
        }

        String productUrl = (String) item.getOrDefault("itemWebUrl", "");

        return new NormalizedProduct(
                SourceType.API,
                SOURCE_NAME,
                itemId,
                title,
                "",
                "",
                "",
                price,
                currency,
                "",
                merchantId,
                sellerName.isEmpty() ? SOURCE_NAME : sellerName,
                merchantRating,
                null,
                null,
                imageUrls,
                productUrl,
                Map.of(),
                Instant.now()
        );
    }

    /**
     * Extracts a 0-1 merchant rating from eBay seller data.
     * Uses feedbackPercentage (0-100) / 100 if available.
     * Otherwise uses feedbackScore with a provisional heuristic:
     * min(score, 500000) / 500000, clamped to 0-1.
     * This is provisional — eBay feedback scores vary widely and a more
     * sophisticated normalisation should replace this heuristic.
     */
    private Double extractMerchantRating(Map<String, Object> seller) {
        Object feedbackPct = seller.get("feedbackPercentage");
        if (feedbackPct != null) {
            double pct = Double.parseDouble(feedbackPct.toString());
            return pct / 100.0;
        }

        Object feedbackScore = seller.get("feedbackScore");
        if (feedbackScore != null) {
            double score = Double.parseDouble(feedbackScore.toString());
            return Math.min(score, 500000.0) / 500000.0;
        }

        return null;
    }

    private UUID lookupMerchantId() {
        return merchantRepository.findAllByIsApproved(true).stream()
                .filter(m -> "eBay".equalsIgnoreCase(m.getName()))
                .map(MerchantJpaEntity::getId)
                .findFirst()
                .orElse(null);
    }
}
