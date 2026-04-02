package com.aisleon.merchant.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantJpaEntity, UUID> {

    List<MerchantJpaEntity> findBySourceType(String sourceType);

    List<MerchantJpaEntity> findAllByIsApproved(Boolean isApproved);
}
