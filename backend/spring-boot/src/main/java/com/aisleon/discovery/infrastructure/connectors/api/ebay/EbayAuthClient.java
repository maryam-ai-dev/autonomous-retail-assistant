package com.aisleon.discovery.infrastructure.connectors.api.ebay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class EbayAuthClient {

    private static final String TOKEN_URL = "https://api.ebay.com/identity/v1/oauth2/token";
    private static final long REFRESH_BUFFER_SECONDS = 60;

    private final String appId;
    private final String clientSecret;
    private final RestTemplate restTemplate;

    private String cachedToken;
    private Instant tokenExpiresAt;

    public EbayAuthClient(
            @Value("${ebay.app-id:}") String appId,
            @Value("${ebay.client-secret:}") String clientSecret) {
        this.appId = appId;
        this.clientSecret = clientSecret;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Returns a valid eBay OAuth2 access token, refreshing if expired or near expiry.
     */
    public synchronized String getAccessToken() {
        if (cachedToken != null && tokenExpiresAt != null
                && Instant.now().plusSeconds(REFRESH_BUFFER_SECONDS).isBefore(tokenExpiresAt)) {
            return cachedToken;
        }
        return refreshToken();
    }

    @SuppressWarnings("unchecked")
    private String refreshToken() {
        if (appId == null || appId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "eBay credentials not configured — set EBAY_APP_ID and EBAY_CLIENT_SECRET");
        }

        String credentials = Base64.getEncoder()
                .encodeToString((appId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "https://api.ebay.com/oauth/api_scope");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new IllegalStateException("eBay token response did not contain access_token");
        }

        cachedToken = (String) responseBody.get("access_token");
        int expiresIn = (int) responseBody.get("expires_in");
        tokenExpiresAt = Instant.now().plusSeconds(expiresIn);

        return cachedToken;
    }
}
