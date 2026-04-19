package com.aisleon.basket.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketIntentRepository extends JpaRepository<BasketIntentJpaEntity, UUID> {
}
