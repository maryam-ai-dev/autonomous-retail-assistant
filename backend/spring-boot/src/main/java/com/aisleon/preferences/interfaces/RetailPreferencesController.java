package com.aisleon.preferences.interfaces;

import com.aisleon.preferences.application.RetailPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
@Validated
public class RetailPreferencesController {

    private final RetailPreferencesService preferencesService;

    public RetailPreferencesController(RetailPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping
    public ResponseEntity<GetPreferencesResponse> getPreferences(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(preferencesService.getPreferences(userId));
    }

    @PutMapping
    public ResponseEntity<GetPreferencesResponse> updatePreferences(
            Authentication authentication,
            @RequestBody UpdatePreferencesRequest request) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(preferencesService.updatePreferences(userId, request));
    }
}
