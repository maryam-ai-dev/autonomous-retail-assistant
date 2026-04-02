package com.aisleon.preferences.application;

import com.aisleon.preferences.domain.RetailPreferences;
import com.aisleon.preferences.infrastructure.RetailPreferencesJpaEntity;
import com.aisleon.preferences.infrastructure.RetailPreferencesMapper;
import com.aisleon.preferences.infrastructure.RetailPreferencesRepository;
import com.aisleon.preferences.interfaces.GetPreferencesResponse;
import com.aisleon.preferences.interfaces.UpdatePreferencesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RetailPreferencesService {

    private final RetailPreferencesRepository repository;

    public RetailPreferencesService(RetailPreferencesRepository repository) {
        this.repository = repository;
    }

    public GetPreferencesResponse getPreferences(UUID userId) {
        RetailPreferencesJpaEntity entity = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user"));

        RetailPreferences domain = RetailPreferencesMapper.toDomain(entity);
        return toResponse(domain);
    }

    @Transactional
    public GetPreferencesResponse updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        RetailPreferencesJpaEntity entity = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user"));

        RetailPreferences domain = RetailPreferencesMapper.toDomain(entity);

        if (request.getBudgetCap() != null) domain.setBudgetCap(request.getBudgetCap());
        if (request.getPreferredBrands() != null) domain.setPreferredBrands(request.getPreferredBrands());
        if (request.getBlockedBrands() != null) domain.setBlockedBrands(request.getBlockedBrands());
        if (request.getBlockedCategories() != null) domain.setBlockedCategories(request.getBlockedCategories());
        if (request.getAllowSubstitutions() != null) domain.setAllowSubstitutions(request.getAllowSubstitutions());
        if (request.getApprovalThreshold() != null) domain.setApprovalThreshold(request.getApprovalThreshold());
        if (request.getMaxSubstitutionPriceDelta() != null) domain.setMaxSubstitutionPriceDelta(request.getMaxSubstitutionPriceDelta());

        RetailPreferencesJpaEntity updatedEntity = RetailPreferencesMapper.toEntity(domain);
        updatedEntity.setUpdatedAt(LocalDateTime.now());
        updatedEntity.setCreatedAt(entity.getCreatedAt());
        repository.save(updatedEntity);

        return toResponse(domain);
    }

    @Transactional
    public void createDefaultPreferences(UUID userId) {
        RetailPreferencesJpaEntity entity = RetailPreferencesJpaEntity.builder()
                .userId(userId)
                .budgetCap(new BigDecimal("100.00"))
                .preferredBrands(List.of())
                .blockedBrands(List.of())
                .blockedCategories(List.of())
                .allowSubstitutions(true)
                .approvalThreshold(new BigDecimal("50.00"))
                .maxSubstitutionPriceDelta(new BigDecimal("10.00"))
                .build();
        repository.save(entity);
    }

    private GetPreferencesResponse toResponse(RetailPreferences domain) {
        return GetPreferencesResponse.builder()
                .userId(domain.getUserId().toString())
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
