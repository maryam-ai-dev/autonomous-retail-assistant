package com.aisleon.preferences.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Pure domain aggregate root for retail preferences.
 * No JPA or Spring imports allowed.
 */
public class RetailPreferences {

    private final UUID id;
    private final UUID userId;
    private BigDecimal budgetCap;
    private List<String> preferredBrands;
    private List<String> blockedBrands;
    private List<String> blockedCategories;
    private boolean allowSubstitutions;
    private BigDecimal approvalThreshold;
    private BigDecimal maxSubstitutionPriceDelta;

    public RetailPreferences(UUID id,
                             UUID userId,
                             BigDecimal budgetCap,
                             List<String> preferredBrands,
                             List<String> blockedBrands,
                             List<String> blockedCategories,
                             boolean allowSubstitutions,
                             BigDecimal approvalThreshold,
                             BigDecimal maxSubstitutionPriceDelta) {
        this.id = id;
        this.userId = userId;
        this.budgetCap = budgetCap;
        this.preferredBrands = preferredBrands;
        this.blockedBrands = blockedBrands;
        this.blockedCategories = blockedCategories;
        this.allowSubstitutions = allowSubstitutions;
        this.approvalThreshold = approvalThreshold;
        this.maxSubstitutionPriceDelta = maxSubstitutionPriceDelta;
    }

    public boolean isSubstitutionAllowed() {
        return allowSubstitutions;
    }

    /**
     * Returns true if the given amount exceeds the user's approval threshold.
     */
    public boolean requiresApproval(BigDecimal amount) {
        if (approvalThreshold == null) {
            return false;
        }
        return amount.compareTo(approvalThreshold) > 0;
    }

    public boolean isBrandBlocked(String brand) {
        if (blockedBrands == null || blockedBrands.isEmpty()) {
            return false;
        }
        return blockedBrands.stream()
                .anyMatch(b -> b.equalsIgnoreCase(brand));
    }

    public boolean isCategoryBlocked(String category) {
        if (blockedCategories == null || blockedCategories.isEmpty()) {
            return false;
        }
        return blockedCategories.stream()
                .anyMatch(c -> c.equalsIgnoreCase(category));
    }

    /**
     * Returns true if the price delta exceeds the maximum allowed for substitutions.
     */
    public boolean substitutionExceedsDelta(BigDecimal priceDelta) {
        if (maxSubstitutionPriceDelta == null) {
            return false;
        }
        return priceDelta.compareTo(maxSubstitutionPriceDelta) > 0;
    }

    // Getters

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getBudgetCap() { return budgetCap; }
    public List<String> getPreferredBrands() { return preferredBrands; }
    public List<String> getBlockedBrands() { return blockedBrands; }
    public List<String> getBlockedCategories() { return blockedCategories; }
    public boolean getAllowSubstitutions() { return allowSubstitutions; }
    public BigDecimal getApprovalThreshold() { return approvalThreshold; }
    public BigDecimal getMaxSubstitutionPriceDelta() { return maxSubstitutionPriceDelta; }

    // Setters for mutable fields

    public void setBudgetCap(BigDecimal budgetCap) { this.budgetCap = budgetCap; }
    public void setPreferredBrands(List<String> preferredBrands) { this.preferredBrands = preferredBrands; }
    public void setBlockedBrands(List<String> blockedBrands) { this.blockedBrands = blockedBrands; }
    public void setBlockedCategories(List<String> blockedCategories) { this.blockedCategories = blockedCategories; }
    public void setAllowSubstitutions(boolean allowSubstitutions) { this.allowSubstitutions = allowSubstitutions; }
    public void setApprovalThreshold(BigDecimal approvalThreshold) { this.approvalThreshold = approvalThreshold; }
    public void setMaxSubstitutionPriceDelta(BigDecimal maxSubstitutionPriceDelta) { this.maxSubstitutionPriceDelta = maxSubstitutionPriceDelta; }
}
