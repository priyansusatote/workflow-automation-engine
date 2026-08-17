package com.priyansu.workflow.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUserPrincipalTest {

    @Test
    @DisplayName("Should return the authorities supplied to the principal")
    void shouldReturnSuppliedAuthorities() {
        var authority = new SimpleGrantedAuthority("ROLE_USER");
        var principal = new JwtUserPrincipal(UUID.randomUUID(), "user@example.com", List.of(authority));

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Should preserve multiple supplied authorities")
    void shouldPreserveMultipleAuthorities() {
        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        var principal = new JwtUserPrincipal(UUID.randomUUID(), "admin@example.com", authorities);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should retain the existing UserDetails principal behavior")
    void shouldRetainExistingPrincipalBehavior() {
        UUID userId = UUID.randomUUID();
        var principal = new JwtUserPrincipal(
                userId,
                "user@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.getUsername()).isEqualTo("user@example.com");
        assertThat(principal.getPassword()).isNull();
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }
}
