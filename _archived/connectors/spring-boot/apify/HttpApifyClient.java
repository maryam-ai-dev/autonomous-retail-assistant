package com.aisleon.scraping.apify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpApifyClient implements ApifyClient {

    private static final String BASE = "https://api.apify.com/v2";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String apiKey;

    public HttpApifyClient(@Value("${apify.api-key:${APIFY_API_KEY:}}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<Map<String, Object>> runSync(String actorId, Map<String, Object> input) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApifyException("APIFY_API_KEY not configured", false, 0);
        }
        try {
            byte[] body = mapper.writeValueAsBytes(input);
            URI uri = URI.create(
                    BASE + "/acts/" + actorId + "/run-sync-get-dataset-items?token=" + apiKey);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .timeout(TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
            HttpResponse<byte[]> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status >= 400) {
                throw new ApifyException(
                        "Apify returned HTTP " + status, false, status);
            }
            return mapper.readValue(response.body(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (HttpTimeoutException e) {
            throw new ApifyException("Apify request timed out", true, 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApifyException("Apify call interrupted", e);
        } catch (ApifyException e) {
            throw e;
        } catch (Exception e) {
            throw new ApifyException("Apify call failed: " + e.getMessage(), e);
        }
    }
}
