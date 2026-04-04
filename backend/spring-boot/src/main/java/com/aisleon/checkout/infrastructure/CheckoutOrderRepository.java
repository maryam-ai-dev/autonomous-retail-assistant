package com.aisleon.checkout.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckoutOrderRepository extends JpaRepository<CheckoutOrderJpaEntity, UUID> {

    List<CheckoutOrderJpaEntity> findByUserId(UUID userId);
}
