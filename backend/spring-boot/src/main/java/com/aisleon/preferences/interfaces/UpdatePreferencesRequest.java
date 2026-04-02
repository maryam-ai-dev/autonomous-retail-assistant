package com.aisleon.preferences.interfaces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {

    private BigDecimal budgetCap;
    private List<String> preferredBrands;
    private List<String> blockedBrands;
    private List<String> blockedCategories;
    private Boolean allowSubstitutions;
    private BigDecimal approvalThreshold;
    private BigDecimal maxSubstitutionPriceDelta;
}
