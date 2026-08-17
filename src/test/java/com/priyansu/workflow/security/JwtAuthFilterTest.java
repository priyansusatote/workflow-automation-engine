package com.priyansu.workflow.security;

import com.priyansu.workflow.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtService jwtService;
    @Mock TokenBlacklistService tokenBlacklistService;
    @Mock FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should leave requests without bearer authentication to the filter chain")
    void shouldPassThroughRequestWithoutBearerToken() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, tokenBlacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, tokenBlacklistService);
    }

    @Test
    @DisplayName("Should establish a principal and authorities from a valid bearer token")
    void shouldEstablishPrincipalFromValidToken() throws Exception {
        UUID userId = UUID.randomUUID();
        Claims claims = claims(userId, "user@example.com", "USER", "jti-1");
        when(jwtService.extractAllClaims("valid-token")).thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-1")).thenReturn(false);

        JwtAuthFilter filter = new JwtAuthFilter(jwtService, tokenBlacklistService);
        MockHttpServletRequest request = requestWithBearer("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(JwtUserPrincipal.class);
        assertThat(((JwtUserPrincipal) authentication.getPrincipal()).userId()).isEqualTo(userId);
        assertThat(authentication.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should reject an invalid bearer token with HTTP 401")
    void shouldRejectInvalidBearerToken() throws Exception {
        when(jwtService.extractAllClaims("invalid-token"))
                .thenThrow(new JwtException("invalid"));
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, tokenBlacklistService);
        MockHttpServletRequest request = requestWithBearer("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid or expired token");
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("Should reject a blacklisted bearer token with HTTP 401")
    void shouldRejectBlacklistedToken() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-2");
        when(jwtService.extractAllClaims("blacklisted-token"))
                .thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-2")).thenReturn(true);
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, tokenBlacklistService);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(requestWithBearer("blacklisted-token"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(filterChain);
    }

    private MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private Claims claims(UUID userId, String email, String role, String jti) {
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn(jti);
        when(claims.getSubject()).thenReturn(email);
        when(claims.get("userId")).thenReturn(userId.toString());
        when(claims.get("role")).thenReturn(role);
        return claims;
    }
}
