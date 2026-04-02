package com.aisleon.identity.auth;

import com.aisleon.identity.auth.dto.LoginRequest;
import com.aisleon.identity.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_withValidData_returnsCreatedWithToken() throws Exception {
        var request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .username("testuser")
                .displayName("Test User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void register_withDuplicateEmail_returnsBadRequest() throws Exception {
        var request = RegisterRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .username("user1")
                .displayName("User One")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var duplicateRequest = RegisterRequest.builder()
                .email("duplicate@example.com")
                .password("password456")
                .username("user2")
                .displayName("User Two")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returnsOkWithToken() throws Exception {
        var registerRequest = RegisterRequest.builder()
                .email("login@example.com")
                .password("password123")
                .username("loginuser")
                .displayName("Login User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        var loginRequest = LoginRequest.builder()
                .email("login@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        var registerRequest = RegisterRequest.builder()
                .email("wrongpw@example.com")
                .password("password123")
                .username("wrongpwuser")
                .displayName("Wrong PW User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        var loginRequest = LoginRequest.builder()
                .email("wrongpw@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
