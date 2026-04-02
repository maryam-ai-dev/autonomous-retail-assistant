package com.aisleon.preferences.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "retail_preferences")
public class RetailPreferencesJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "budget_cap", precision = 10, scale = 2)
    private BigDecimal budgetCap;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_brands")
    private List<String> preferredBrands;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "blocked_brands")
    private List<String> blockedBrands;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "blocked_categories")
    private List<String> blockedCategories;

    @Column(name = "allow_substitutions")
    @Builder.Default
    private Boolean allowSubstitutions = true;

    @Column(name = "approval_threshold", precision = 10, scale = 2)
    private BigDecimal approvalThreshold;

    @Column(name = "max_substitution_price_delta", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal maxSubstitutionPriceDelta = new BigDecimal("10.00");

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
