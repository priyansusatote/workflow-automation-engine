package com.priyansu.workflow.security;

import com.priyansu.workflow.entity.User;
import com.priyansu.workflow.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(
                "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)
        );
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);
    }

    @Test
    @DisplayName("Should generate a token containing the user identity and role")
    void shouldGenerateTokenWithExpectedClaims() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .role(Role.USER)
                .password("hashed")
                .build();

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo("user@example.com");
        assertThat(claims.get("userId", String.class)).isEqualTo(userId.toString());
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractJti(token)).isNotBlank();
    }

    @Test
    @DisplayName("Should reject an invalid JWT")
    void shouldRejectInvalidToken() {
        assertThatThrownBy(() -> jwtService.extractAllClaims("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should reject an expired JWT")
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .role(Role.USER)
                .password("hashed")
                .build();

        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.extractAllClaims(token))
                .isInstanceOf(JwtException.class);
    }
}
