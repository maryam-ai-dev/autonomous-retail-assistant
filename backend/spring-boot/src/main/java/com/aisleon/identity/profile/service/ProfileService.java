package com.aisleon.identity.profile.service;

import com.aisleon.identity.auth.repository.User;
import com.aisleon.identity.auth.repository.UserRepository;
import com.aisleon.identity.profile.dto.ProfileResponse;
import com.aisleon.identity.profile.dto.UpdateProfileRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ProfileResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .build();
    }

    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setDisplayName(request.getDisplayName());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ProfileResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .build();
    }
}
