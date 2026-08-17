package com.priyansu.workflow.security;

import com.priyansu.workflow.dto.AuthRequest;
import com.priyansu.workflow.dto.LoginResponse;
import com.priyansu.workflow.entity.RefreshToken;
import com.priyansu.workflow.entity.User;
import com.priyansu.workflow.entity.enums.Role;
import com.priyansu.workflow.exception.InvalidCredentialsException;
import com.priyansu.workflow.exception.UserAlreadyExistsException;
import com.priyansu.workflow.repository.RefreshTokenRepository;
import com.priyansu.workflow.repository.UserRepository;
import com.priyansu.workflow.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock TokenBlacklistService tokenBlacklistService;
    @InjectMocks AuthService authService;

    @Test
    @DisplayName("Should authenticate valid credentials and persist a refresh token")
    void shouldLoginSuccessfully() {
        User user = user();
        AuthRequest request = request();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");

        LoginResponse response = authService.login(request);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject an unknown login email")
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request()))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email");
        verifyNoInteractions(passwordEncoder, jwtService, refreshTokenRepository);
    }

    @Test
    @DisplayName("Should reject an incorrect password")
    void shouldRejectIncorrectPassword() {
        User user = user();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request()))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid password");
        verifyNoInteractions(jwtService, refreshTokenRepository);
    }

    @Test
    @DisplayName("Should reject a duplicate signup")
    void shouldRejectDuplicateSignup() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user()));

        assertThatThrownBy(() -> authService.signup(request()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject an invalid refresh token")
    void shouldRejectInvalidRefreshToken() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("missing"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid refresh token");
        verifyNoInteractions(jwtService);
    }

    private AuthRequest request() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password1!");
        return request;
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .build();
    }
}
