package com.aisleon.policy.domain;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * Value object representing the purchase policy constraints for a user.
 */
public class PurchasePolicy {

    private final BigDecimal maxSpendWithoutApproval;
    private final Set<UUID> approvedMerchantIds;
    private final Set<String> approvedMerchantNames;
    private final boolean allowSubstitutions;
    private final BigDecimal maxSubstitutionPriceDelta;

    public PurchasePolicy(BigDecimal maxSpendWithoutApproval,
                          Set<UUID> approvedMerchantIds,
                          Set<String> approvedMerchantNames,
                          boolean allowSubstitutions,
                          BigDecimal maxSubstitutionPriceDelta) {
        this.maxSpendWithoutApproval = maxSpendWithoutApproval;
        this.approvedMerchantIds = approvedMerchantIds;
        this.approvedMerchantNames = approvedMerchantNames;
        this.allowSubstitutions = allowSubstitutions;
        this.maxSubstitutionPriceDelta = maxSubstitutionPriceDelta;
    }

    public BigDecimal getMaxSpendWithoutApproval() { return maxSpendWithoutApproval; }
    public Set<UUID> getApprovedMerchantIds() { return approvedMerchantIds; }
    public Set<String> getApprovedMerchantNames() { return approvedMerchantNames; }
    public boolean isAllowSubstitutions() { return allowSubstitutions; }
    public BigDecimal getMaxSubstitutionPriceDelta() { return maxSubstitutionPriceDelta; }
}
