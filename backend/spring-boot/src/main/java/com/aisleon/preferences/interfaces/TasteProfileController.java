package com.aisleon.preferences.interfaces;

import com.aisleon.preferences.application.TasteProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Taste profile")
public class TasteProfileController {

    private final TasteProfileService service;

    public TasteProfileController(TasteProfileService service) {
        this.service = service;
    }

    @Operation(summary = "Get the authenticated user's taste profile")
    @GetMapping("/taste-profile")
    public ResponseEntity<TasteProfileResponse> get(Authentication auth) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(service.getOrCreate(userId));
    }

    @Operation(summary = "Partial update of the authenticated user's taste profile")
    @PutMapping("/taste-profile")
    public ResponseEntity<TasteProfileResponse> update(
            Authentication auth, @RequestBody UpdateTasteProfileRequest request) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(service.update(userId, request));
    }

    @Operation(summary = "Check whether the user's clothing profile is complete")
    @GetMapping("/clothing-profile/complete")
    public ResponseEntity<ClothingProfileCompleteResponse> clothingComplete(Authentication auth) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(service.clothingComplete(userId));
    }
}
