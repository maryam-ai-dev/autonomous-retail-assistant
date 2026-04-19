package com.aisleon.basket.infrastructure;

import com.aisleon.basket.BasketStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketRepository extends JpaRepository<BasketJpaEntity, UUID> {

    Optional<BasketJpaEntity> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, BasketStatus status);
}
