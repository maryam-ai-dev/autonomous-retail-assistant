package com.aisleon.merchant.infrastructure;

import com.aisleon.merchant.domain.Merchant;
import com.aisleon.merchant.domain.SourceType;

import java.math.BigDecimal;

/**
 * Maps between the Merchant domain object and the JPA entity.
 * This is the only place where domain ↔ JPA translation happens.
 */
public class MerchantMapper {

    private MerchantMapper() {
    }

    public static Merchant toDomain(MerchantJpaEntity entity) {
        return new Merchant(
                entity.getId(),
                entity.getName(),
                entity.getSourceType() != null ? SourceType.valueOf(entity.getSourceType()) : null,
                entity.getIsApproved() != null && entity.getIsApproved(),
                entity.getTrustScore() != null ? entity.getTrustScore().doubleValue() : 0.0,
                entity.getApiKeyRef(),
                entity.getBaseUrl()
        );
    }

    public static MerchantJpaEntity toEntity(Merchant domain) {
        return MerchantJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .sourceType(domain.getSourceType() != null ? domain.getSourceType().name() : null)
                .isApproved(domain.isApproved())
                .trustScore(BigDecimal.valueOf(domain.getTrustScore()))
                .apiKeyRef(domain.getApiKeyRef())
                .baseUrl(domain.getBaseUrl())
                .build();
    }
}
