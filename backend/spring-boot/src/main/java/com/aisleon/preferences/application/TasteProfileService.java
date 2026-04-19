package com.aisleon.preferences.application;

import com.aisleon.preferences.infrastructure.TasteProfileJpaEntity;
import com.aisleon.preferences.infrastructure.TasteProfileRepository;
import com.aisleon.preferences.interfaces.ClothingProfileCompleteResponse;
import com.aisleon.preferences.interfaces.TasteProfileResponse;
import com.aisleon.preferences.interfaces.UpdateTasteProfileRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TasteProfileService {

    private final TasteProfileRepository repository;

    public TasteProfileService(TasteProfileRepository repository) {
        this.repository = repository;
    }

    public TasteProfileResponse getOrCreate(UUID userId) {
        TasteProfileJpaEntity entity = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(defaultFor(userId)));
        return toResponse(entity);
    }

    @Transactional
    public TasteProfileResponse update(UUID userId, UpdateTasteProfileRequest req) {
        TasteProfileJpaEntity entity = repository.findByUserId(userId)
                .orElseGet(() -> defaultFor(userId));
        if (req.getHalalOnly() != null) entity.setHalalOnly(req.getHalalOnly());
        if (req.getVeganOnly() != null) entity.setVeganOnly(req.getVeganOnly());
        if (req.getVegetarianOnly() != null) entity.setVegetarianOnly(req.getVegetarianOnly());
        if (req.getGlutenFree() != null) entity.setGlutenFree(req.getGlutenFree());
        if (req.getDairyFree() != null) entity.setDairyFree(req.getDairyFree());
        if (req.getOrganicPreferred() != null) entity.setOrganicPreferred(req.getOrganicPreferred());
        if (req.getRetailerAllowList() != null) entity.setRetailerAllowList(req.getRetailerAllowList());
        if (req.getRetailerDenyList() != null) entity.setRetailerDenyList(req.getRetailerDenyList());
        if (req.getPreferredBrands() != null) entity.setPreferredBrands(req.getPreferredBrands());
        if (req.getHouseholdSize() != null) entity.setHouseholdSize(req.getHouseholdSize());
        if (req.getHasChildren() != null) entity.setHasChildren(req.getHasChildren());
        if (req.getHasPets() != null) entity.setHasPets(req.getHasPets());
        if (req.getCleaningScentSensitive() != null)
            entity.setCleaningScentSensitive(req.getCleaningScentSensitive());
        if (req.getTopSize() != null) entity.setTopSize(req.getTopSize());
        if (req.getBottomSize() != null) entity.setBottomSize(req.getBottomSize());
        if (req.getShoeSizeUk() != null) entity.setShoeSizeUk(req.getShoeSizeUk());
        if (req.getDressSize() != null) entity.setDressSize(req.getDressSize());
        if (req.getClothingPreferences() != null)
            entity.setClothingPreferences(req.getClothingPreferences());
        entity.setUpdatedAt(LocalDateTime.now());
        TasteProfileJpaEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    public ClothingProfileCompleteResponse clothingComplete(UUID userId) {
        boolean complete = repository.findByUserId(userId)
                .map(e -> e.getTopSize() != null
                        && e.getBottomSize() != null
                        && e.getShoeSizeUk() != null
                        && e.getDressSize() != null)
                .orElse(false);
        return new ClothingProfileCompleteResponse(complete);
    }

    @Transactional
    public void ensureProfileExists(UUID userId) {
        if (repository.findByUserId(userId).isEmpty()) {
            repository.save(defaultFor(userId));
        }
    }

    private TasteProfileJpaEntity defaultFor(UUID userId) {
        return TasteProfileJpaEntity.builder().userId(userId).build();
    }

    private TasteProfileResponse toResponse(TasteProfileJpaEntity e) {
        return TasteProfileResponse.builder()
                .userId(e.getUserId().toString())
                .halalOnly(e.getHalalOnly())
                .veganOnly(e.getVeganOnly())
                .vegetarianOnly(e.getVegetarianOnly())
                .glutenFree(e.getGlutenFree())
                .dairyFree(e.getDairyFree())
                .organicPreferred(e.getOrganicPreferred())
                .retailerAllowList(e.getRetailerAllowList())
                .retailerDenyList(e.getRetailerDenyList())
                .preferredBrands(e.getPreferredBrands())
                .householdSize(e.getHouseholdSize())
                .hasChildren(e.getHasChildren())
                .hasPets(e.getHasPets())
                .cleaningScentSensitive(e.getCleaningScentSensitive())
                .topSize(e.getTopSize())
                .bottomSize(e.getBottomSize())
                .shoeSizeUk(e.getShoeSizeUk())
                .dressSize(e.getDressSize())
                .clothingPreferences(e.getClothingPreferences())
                .build();
    }
}
