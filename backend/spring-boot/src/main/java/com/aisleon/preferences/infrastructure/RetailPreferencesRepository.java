package com.aisleon.preferences.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetailPreferencesRepository extends JpaRepository<RetailPreferencesJpaEntity, UUID> {

    Optional<RetailPreferencesJpaEntity> findByUserId(UUID userId);
}
