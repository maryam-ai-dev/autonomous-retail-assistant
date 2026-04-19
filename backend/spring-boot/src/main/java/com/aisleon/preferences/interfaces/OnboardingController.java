package com.aisleon.preferences.interfaces;

import com.aisleon.preferences.application.TasteProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@Tag(name = "Onboarding")
public class OnboardingController {

    private final TasteProfileService tasteProfileService;

    public OnboardingController(TasteProfileService tasteProfileService) {
        this.tasteProfileService = tasteProfileService;
    }

    @Operation(summary = "Mark onboarding complete — idempotent; ensures a taste profile exists")
    @PostMapping("/complete")
    public ResponseEntity<Void> complete(Authentication auth) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        tasteProfileService.ensureProfileExists(userId);
        return ResponseEntity.noContent().build();
    }
}
