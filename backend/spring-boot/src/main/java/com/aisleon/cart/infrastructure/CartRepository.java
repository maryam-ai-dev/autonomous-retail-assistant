package com.aisleon.cart.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartJpaEntity, UUID> {

    Optional<CartJpaEntity> findByUserId(UUID userId);

    Optional<CartJpaEntity> findByUserIdAndStatus(UUID userId, String status);
}
