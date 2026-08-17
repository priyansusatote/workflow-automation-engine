package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.LoginResponse;
import com.priyansu.workflow.exception.GlobalExceptionHandler;
import com.priyansu.workflow.exception.InvalidCredentialsException;
import com.priyansu.workflow.exception.UserAlreadyExistsException;
import com.priyansu.workflow.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerSecurityMvcTest {

    private final AuthService authService = mock(AuthService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("Should return an access token and HttpOnly refresh cookie after login")
    void shouldLoginWithRefreshCookie() throws Exception {
        when(authService.login(any())).thenReturn(
                new LoginResponse(UUID.randomUUID(), "access-token", "refresh-token")
        );

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("Should reject refresh when the refresh cookie is missing")
    void shouldRejectMissingRefreshCookie() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token missing"));

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should clear the refresh cookie during logout")
    void shouldLogoutAndClearRefreshCookie() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer access-token")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(authService).logout("refresh-token", "access-token");
    }

    @Test
    @DisplayName("Should map invalid authentication credentials to unauthorized")
    void shouldMapInvalidCredentials() throws Exception {
        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Invalid password"));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    @DisplayName("Should map duplicate user registration to conflict")
    void shouldMapDuplicateUserToConflict() throws Exception {
        doThrow(new UserAlreadyExistsException("User already exists"))
                .when(authService).signup(any());

        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    @DisplayName("Should map invalid authentication input to bad request")
    void shouldMapInvalidAuthRequestToBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "email: must be a well-formed email address, password: size must be between 8 and 2147483647"));

        verifyNoInteractions(authService);
    }
}
