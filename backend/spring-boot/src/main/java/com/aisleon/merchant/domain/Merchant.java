package com.aisleon.merchant.domain;

import java.util.UUID;

/**
 * Pure domain aggregate root for merchants. No JPA or Spring imports.
 */
public class Merchant {

    private final UUID id;
    private final String name;
    private final SourceType sourceType;
    private boolean approved;
    private double trustScore;
    private final String apiKeyRef;
    private final String baseUrl;

    public Merchant(UUID id,
                    String name,
                    SourceType sourceType,
                    boolean approved,
                    double trustScore,
                    String apiKeyRef,
                    String baseUrl) {
        this.id = id;
        this.name = name;
        this.sourceType = sourceType;
        this.approved = approved;
        this.trustScore = trustScore;
        this.apiKeyRef = apiKeyRef;
        this.baseUrl = baseUrl;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isTrusted(double threshold) {
        return trustScore >= threshold;
    }

    public void approve() {
        this.approved = true;
    }

    public void block() {
        this.approved = false;
    }

    // Getters

    public UUID getId() { return id; }
    public String getName() { return name; }
    public SourceType getSourceType() { return sourceType; }
    public double getTrustScore() { return trustScore; }
    public String getApiKeyRef() { return apiKeyRef; }
    public String getBaseUrl() { return baseUrl; }
}
