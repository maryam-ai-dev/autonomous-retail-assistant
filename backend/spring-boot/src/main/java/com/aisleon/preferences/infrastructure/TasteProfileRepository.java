package com.aisleon.preferences.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TasteProfileRepository extends JpaRepository<TasteProfileJpaEntity, UUID> {

    Optional<TasteProfileJpaEntity> findByUserId(UUID userId);
}
