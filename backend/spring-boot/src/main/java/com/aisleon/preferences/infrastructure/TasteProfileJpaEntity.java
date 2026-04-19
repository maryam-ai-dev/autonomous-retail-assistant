package com.aisleon.preferences.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "taste_profiles")
public class TasteProfileJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "halal_only", nullable = false)
    @Builder.Default
    private Boolean halalOnly = false;

    @Column(name = "vegan_only", nullable = false)
    @Builder.Default
    private Boolean veganOnly = false;

    @Column(name = "vegetarian_only", nullable = false)
    @Builder.Default
    private Boolean vegetarianOnly = false;

    @Column(name = "gluten_free", nullable = false)
    @Builder.Default
    private Boolean glutenFree = false;

    @Column(name = "dairy_free", nullable = false)
    @Builder.Default
    private Boolean dairyFree = false;

    @Column(name = "organic_preferred", nullable = false)
    @Builder.Default
    private Boolean organicPreferred = false;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "retailer_allow_list")
    @Builder.Default
    private List<String> retailerAllowList = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "retailer_deny_list")
    @Builder.Default
    private List<String> retailerDenyList = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_brands")
    @Builder.Default
    private List<String> preferredBrands = new ArrayList<>();

    @Column(name = "household_size", nullable = false)
    @Builder.Default
    private Integer householdSize = 1;

    @Column(name = "has_children", nullable = false)
    @Builder.Default
    private Boolean hasChildren = false;

    @Column(name = "has_pets", nullable = false)
    @Builder.Default
    private Boolean hasPets = false;

    @Column(name = "cleaning_scent_sensitive", nullable = false)
    @Builder.Default
    private Boolean cleaningScentSensitive = false;

    @Column(name = "top_size")
    private String topSize;

    @Column(name = "bottom_size")
    private String bottomSize;

    @Column(name = "shoe_size_uk", precision = 3, scale = 1)
    private BigDecimal shoeSizeUk;

    @Column(name = "dress_size")
    private String dressSize;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "clothing_preferences")
    @Builder.Default
    private List<String> clothingPreferences = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
