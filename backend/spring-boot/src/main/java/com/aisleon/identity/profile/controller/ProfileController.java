package com.aisleon.identity.profile.controller;

import com.aisleon.identity.profile.dto.ProfileResponse;
import com.aisleon.identity.profile.dto.UpdateProfileRequest;
import com.aisleon.identity.profile.service.ProfileService;
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
@RequestMapping("/api/profile")
@Validated
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
