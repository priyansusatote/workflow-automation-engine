package com.priyansu.workflow.security;

import com.priyansu.workflow.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7).trim(); //trim extra spaces

            // ✅ Only process if SecurityContext is empty (avoid re-processing)
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtService.extractAllClaims(token);

            String jti = claims.getId();
            if (tokenBlacklistService.isBlacklisted(jti)) {
                throw new JwtException("Token blacklisted");
            }

            String email = claims.getSubject();
            UUID userId = UUID.fromString(claims.get("userId").toString());
            String role = claims.get("role").toString();

            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            JwtUserPrincipal principal =
                    new JwtUserPrincipal(userId, email, authorities);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Attaches extra HTTP request metadata (IP address, session ID) to the authentication token — used by Spring Security for audit logging, security event tracking, and fraud detection tools like Spring Session. Not required for basic JWT auth, but good practice for production systems.

            SecurityContextHolder.getContext().setAuthentication(authToken);


        } catch (JwtException | IllegalArgumentException e) {

            // 🔥 Proper failure handling
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"status":401,"error":"Unauthorized","message":"Invalid or expired token"}
            """);
            return;
        }

        filterChain.doFilter(request, response);
    }
}