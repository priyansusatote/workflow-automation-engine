package com.priyansu.workflow.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

//Spring Security does NOT understand your User entity directly ->So this class is: Adapter / Wrapper between your User and Spring Security
//JWT token -> Extract user info-> Create JwtUserPrincipal-> Spring Security uses it
public record JwtUserPrincipal(
        UUID userId,
        String email,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
