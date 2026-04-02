package com.aisleon.discovery.interfaces;

import com.aisleon.discovery.application.ConnectorSelectionService;
import com.aisleon.discovery.domain.DiscoveryResult;
import com.aisleon.preferences.application.RetailPreferencesService;
import com.aisleon.preferences.domain.RetailPreferences;
import com.aisleon.preferences.infrastructure.RetailPreferencesJpaEntity;
import com.aisleon.preferences.infrastructure.RetailPreferencesMapper;
import com.aisleon.preferences.infrastructure.RetailPreferencesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/discovery")
@Validated
public class DiscoveryController {

    private final ConnectorSelectionService connectorSelectionService;
    private final RetailPreferencesRepository preferencesRepository;

    public DiscoveryController(ConnectorSelectionService connectorSelectionService,
                               RetailPreferencesRepository preferencesRepository) {
        this.connectorSelectionService = connectorSelectionService;
        this.preferencesRepository = preferencesRepository;
    }

    @PostMapping("/search")
    public ResponseEntity<DiscoveryResult> search(
            Authentication authentication,
            @Validated @RequestBody DiscoveryRequest request) {

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());

        RetailPreferencesJpaEntity entity = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Preferences not found for user"));
        RetailPreferences preferences = RetailPreferencesMapper.toDomain(entity);

        DiscoveryResult result = connectorSelectionService.discover(
                request.getQuery(), preferences);

        return ResponseEntity.ok(result);
    }
}
