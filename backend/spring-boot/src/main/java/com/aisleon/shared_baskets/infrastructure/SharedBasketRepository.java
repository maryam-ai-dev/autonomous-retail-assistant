package com.aisleon.shared_baskets.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedBasketRepository
        extends JpaRepository<SharedBasketJpaEntity, UUID> {

    Optional<SharedBasketJpaEntity> findByBasketId(UUID basketId);

    Optional<SharedBasketJpaEntity> findByShareId(String shareId);

    boolean existsByShareId(String shareId);
}
