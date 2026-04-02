package com.aisleon.identity.auth.service;

import com.aisleon.identity.auth.dto.AuthResponse;
import com.aisleon.identity.auth.dto.LoginRequest;
import com.aisleon.identity.auth.dto.RegisterRequest;
import com.aisleon.identity.auth.repository.User;
import com.aisleon.identity.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId().toString());

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId().toString())
                .username(saved.getUsername())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId().toString());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .username(user.getUsername())
                .build();
    }
}
