package com.aisleon.scraping.bridge;

import com.aisleon.catalogue.OfferFlag;
import com.aisleon.catalogue.ProductCategory;
import com.aisleon.catalogue.ProductSubcategory;
import com.aisleon.catalogue.RawScraperProduct;
import com.aisleon.catalogue.Retailer;
import com.aisleon.scraping.ConnectorUnavailableException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScraperBridgeClient {

    private static final Logger log = LoggerFactory.getLogger(ScraperBridgeClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String fastapiBaseUrl;

    public ScraperBridgeClient(
            @Value("${python-ai-service.url:http://localhost:8001}") String fastapiBaseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.fastapiBaseUrl = fastapiBaseUrl;
    }

    public List<RawScraperProduct> search(Retailer retailer, String query, int maxResults) {
        Map<String, Object> body = Map.of(
                "query", query,
                "retailer", retailer.name(),
                "maxResults", maxResults);
        try {
            byte[] payload = mapper.writeValueAsBytes(body);
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create(fastapiBaseUrl + "/scrapers/search"))
                    .header("Content-Type", "application/json")
                    .timeout(TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();
            HttpResponse<byte[]> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status >= 500) {
                throw new ConnectorUnavailableException(
                        retailer, "bridge returned HTTP " + status);
            }
            if (status >= 400) {
                log.warn("FastAPI bridge returned {} for {}", status, retailer);
                return List.of();
            }
            List<Map<String, Object>> items = mapper.readValue(
                    response.body(),
                    new TypeReference<List<Map<String, Object>>>() {});
            return items.stream().map(item -> toRaw(item, retailer)).toList();
        } catch (HttpTimeoutException e) {
            throw new ConnectorUnavailableException(retailer, "bridge timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectorUnavailableException(retailer, "bridge interrupted");
        } catch (ConnectorUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorUnavailableException(
                    retailer, "bridge error: " + e.getMessage());
        }
    }

    private static RawScraperProduct toRaw(Map<String, Object> item, Retailer retailer) {
        ProductCategory category = parseEnum(
                item.get("category"), ProductCategory.class, ProductCategory.UNKNOWN);
        ProductSubcategory subcategory = parseEnum(
                item.get("subcategory"),
                ProductSubcategory.class,
                ProductSubcategory.UNKNOWN);
        List<OfferFlag> offers = parseFlags(item.get("offer_flags"));
        Object fetchedAt = item.get("source_fetched_at");
        Instant instant = fetchedAt != null
                ? Instant.parse(fetchedAt.toString())
                : Instant.now();
        return new RawScraperProduct(
                str(item.get("external_id")),
                str(item.get("display_name")),
                str(item.get("brand")),
                category,
                subcategory,
                decimal(item.get("price")),
                Boolean.TRUE.equals(item.get("price_from_text")),
                decimal(item.get("unit_price")),
                str(item.get("unit_basis")),
                str(item.get("size_text")),
                str(item.get("image_url")),
                str(item.get("product_url")),
                Boolean.TRUE.equals(item.getOrDefault("is_available", Boolean.TRUE)),
                Boolean.TRUE.equals(item.getOrDefault("is_basketable", Boolean.TRUE)),
                List.of(),
                offers,
                instant);
    }

    @SuppressWarnings("unchecked")
    private static List<OfferFlag> parseFlags(Object value) {
        if (!(value instanceof List<?> raw)) return List.of();
        List<OfferFlag> out = new ArrayList<>();
        for (Object item : raw) {
            if (item == null) continue;
            try {
                out.add(OfferFlag.valueOf(item.toString()));
            } catch (IllegalArgumentException ignored) {
                // unknown flag from FastAPI
            }
        }
        return out;
    }

    private static <E extends Enum<E>> E parseEnum(Object value, Class<E> type, E defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Enum.valueOf(type, value.toString());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private static String str(Object value) {
        if (value == null) return null;
        String s = value.toString();
        return s.isBlank() ? null : s;
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
