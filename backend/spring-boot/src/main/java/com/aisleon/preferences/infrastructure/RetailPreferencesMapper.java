package com.aisleon.preferences.infrastructure;

import com.aisleon.preferences.domain.RetailPreferences;

import java.util.List;

/**
 * Maps between the RetailPreferences domain object and the JPA entity.
 * This is the only place where domain ↔ JPA translation happens.
 */
public class RetailPreferencesMapper {

    private RetailPreferencesMapper() {
    }

    public static RetailPreferences toDomain(RetailPreferencesJpaEntity entity) {
        return new RetailPreferences(
                entity.getId(),
                entity.getUserId(),
                entity.getBudgetCap(),
                entity.getPreferredBrands() != null ? entity.getPreferredBrands() : List.of(),
                entity.getBlockedBrands() != null ? entity.getBlockedBrands() : List.of(),
                entity.getBlockedCategories() != null ? entity.getBlockedCategories() : List.of(),
                entity.getAllowSubstitutions() != null && entity.getAllowSubstitutions(),
                entity.getApprovalThreshold(),
                entity.getMaxSubstitutionPriceDelta()
        );
    }

    public static RetailPreferencesJpaEntity toEntity(RetailPreferences domain) {
        return RetailPreferencesJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .budgetCap(domain.getBudgetCap())
                .preferredBrands(domain.getPreferredBrands())
                .blockedBrands(domain.getBlockedBrands())
                .blockedCategories(domain.getBlockedCategories())
                .allowSubstitutions(domain.getAllowSubstitutions())
                .approvalThreshold(domain.getApprovalThreshold())
                .maxSubstitutionPriceDelta(domain.getMaxSubstitutionPriceDelta())
                .build();
    }
}
