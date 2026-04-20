package com.aisleon.basket.bridge;

import com.aisleon.basket.ParsedIntent;
import com.aisleon.catalogue.NormalizedProduct;
import com.aisleon.catalogue.ProductCategory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Thin HTTP client for the FastAPI intelligence service.
 *
 * <p>Handles the two endpoints B6.3 orchestrates: {@code /intent/parse} and
 * {@code /basket/generate}. Errors surface as {@link AiBridgeException}; the
 * caller decides whether to bail or fall back.
 */
@Component
public class AiServiceBridgeClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceBridgeClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(25);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public AiServiceBridgeClient(
            @Value("${python-ai-service.url:http://localhost:8001}") String baseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.baseUrl = baseUrl;
    }

    public ParsedIntent parseIntent(String text) {
        Map<String, Object> body = Map.of("text", text);
        Map<String, Object> response = postJson("/intent/parse", body, "intent parse");
        Object intentObj = response.get("intent");
        if (!(intentObj instanceof Map<?, ?> intent)) {
            throw new AiBridgeException("intent/parse returned no intent field");
        }
        return toParsedIntent(text, intent);
    }

    public GeneratedDraftDto generateBasket(BasketGenerateRequestDto request) {
        Map<String, Object> response = postJson(
                "/basket/generate", request.toMap(), "basket generate");
        return GeneratedDraftDto.fromMap(response);
    }

    private Map<String, Object> postJson(String path, Object body, String kind) {
        try {
            byte[] payload = mapper.writeValueAsBytes(body);
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .timeout(TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status >= 400) {
                String snippet = new String(response.body()).trim();
                if (snippet.length() > 200) snippet = snippet.substring(0, 200) + "...";
                throw new AiBridgeException(
                        kind + " returned HTTP " + status + ": " + snippet);
            }
            return mapper.readValue(response.body(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (HttpTimeoutException e) {
            throw new AiBridgeException(kind + " timed out");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiBridgeException(kind + " interrupted");
        } catch (AiBridgeException e) {
            throw e;
        } catch (Exception e) {
            throw new AiBridgeException(kind + " bridge error: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private ParsedIntent toParsedIntent(String rawText, Map<?, ?> intent) {
        BigDecimal budget = null;
        Object budgetObj = intent.get("budget");
        if (budgetObj != null) {
            try {
                budget = new BigDecimal(budgetObj.toString());
            } catch (NumberFormatException e) {
                budget = null;
            }
        }
        ProductCategory category = ProductCategory.HEALTH_BEAUTY;
        Object categoryObj = intent.get("category");
        if (categoryObj != null) {
            try {
                String name = categoryObj.toString();
                if ("MIXED".equals(name)) {
                    category = ProductCategory.HEALTH_BEAUTY;
                } else {
                    category = ProductCategory.valueOf(name);
                }
            } catch (IllegalArgumentException ignored) {
                category = ProductCategory.HEALTH_BEAUTY;
            }
        }
        List<String> tags = toStringList(intent.get("item_hints"));
        tags.addAll(toStringList(intent.get("subcategories")));
        List<String> dietary = toStringList(intent.get("dietary_requirements"));
        boolean halalRequired = dietary.stream().anyMatch(d -> d.equalsIgnoreCase("HALAL"));
        boolean outOfScope = asBool(intent.get("outOfScope"), intent.get("out_of_scope"));
        String outOfScopeReason = asOptionalString(
                intent.get("reason"), intent.get("out_of_scope_reason"));
        return new ParsedIntent(
                rawText,
                budget,
                category,
                tags,
                halalRequired,
                List.of(),
                outOfScope,
                outOfScopeReason);
    }

    private static boolean asBool(Object... candidates) {
        for (Object c : candidates) {
            if (c instanceof Boolean b) return b;
            if (c != null) {
                String s = c.toString();
                if ("true".equalsIgnoreCase(s)) return true;
                if ("false".equalsIgnoreCase(s)) return false;
            }
        }
        return false;
    }

    private static String asOptionalString(Object... candidates) {
        for (Object c : candidates) {
            if (c == null) continue;
            String s = c.toString();
            if (!s.isBlank()) return s;
        }
        return null;
    }

    private static List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (Object item : list) {
            if (item != null) out.add(item.toString());
        }
        return out;
    }

    public static final class BasketGenerateRequestDto {
        private final ParsedIntent intent;
        private final List<NormalizedProduct> candidates;

        public BasketGenerateRequestDto(ParsedIntent intent, List<NormalizedProduct> candidates) {
            this.intent = intent;
            this.candidates = candidates;
        }

        Map<String, Object> toMap() {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("raw_text", intent.rawText() == null ? "" : intent.rawText());
            body.put("budget", intent.budget());
            body.put("category", intent.primaryCategory().name());
            body.put("dietary_requirements",
                    intent.halalRequired() ? List.of("HALAL") : List.of());
            body.put("retailer_hints", List.of());
            body.put("item_hints", intent.tags());
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (NormalizedProduct p : candidates) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("candidate_id", p.externalId());
                row.put("name", p.displayName());
                row.put("brand", p.brand());
                row.put("retailer", p.retailer().name());
                row.put("price", p.price());
                row.put("unit_price", p.unitPrice());
                row.put("unit_basis", p.unitBasis());
                row.put("subcategory", p.subcategory() == null
                        ? null : p.subcategory().name());
                row.put("dietary_tags",
                        p.dietaryTags().stream().map(Enum::name).toList());
                mapped.add(row);
            }
            body.put("candidates", mapped);
            return body;
        }
    }

    public static final class GeneratedDraftDto {
        public final List<GeneratedItemDto> items;
        public final BigDecimal totalCost;
        public final int retryCount;
        public final List<String> warnings;

        public GeneratedDraftDto(
                List<GeneratedItemDto> items,
                BigDecimal totalCost,
                int retryCount,
                List<String> warnings) {
            this.items = items;
            this.totalCost = totalCost;
            this.retryCount = retryCount;
            this.warnings = warnings;
        }

        @SuppressWarnings("unchecked")
        static GeneratedDraftDto fromMap(Map<String, Object> payload) {
            List<GeneratedItemDto> items = new ArrayList<>();
            Object rawItems = payload.get("items");
            if (rawItems instanceof List<?> list) {
                for (Object entry : list) {
                    if (entry instanceof Map<?, ?> m) items.add(GeneratedItemDto.fromMap(m));
                }
            }
            BigDecimal total = BigDecimal.ZERO;
            Object totalObj = payload.get("total_cost");
            if (totalObj != null) {
                try {
                    total = new BigDecimal(totalObj.toString());
                } catch (NumberFormatException ignored) {
                    total = BigDecimal.ZERO;
                }
            }
            int retry = 0;
            Object retryObj = payload.get("retry_count");
            if (retryObj instanceof Number n) retry = n.intValue();
            List<String> warnings = new ArrayList<>();
            Object warnObj = payload.get("warnings");
            if (warnObj instanceof List<?> list) {
                for (Object w : list) if (w != null) warnings.add(w.toString());
            }
            return new GeneratedDraftDto(items, total, retry, warnings);
        }
    }

    public static final class GeneratedItemDto {
        public final String candidateId;
        public final int quantity;
        public final String reasoning;

        public GeneratedItemDto(String candidateId, int quantity, String reasoning) {
            this.candidateId = candidateId;
            this.quantity = quantity;
            this.reasoning = reasoning;
        }

        static GeneratedItemDto fromMap(Map<?, ?> m) {
            Object id = m.get("candidate_id");
            if (id == null) id = m.get("candidateId");
            int qty = 1;
            Object qObj = m.get("quantity");
            if (qObj instanceof Number n) qty = Math.max(1, n.intValue());
            String reasoning = m.get("reasoning") == null
                    ? "" : m.get("reasoning").toString();
            return new GeneratedItemDto(
                    id == null ? "" : id.toString(), qty, reasoning);
        }
    }
}
