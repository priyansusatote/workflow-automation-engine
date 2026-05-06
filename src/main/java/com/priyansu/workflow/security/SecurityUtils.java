package com.priyansu.workflow.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    //get Current-logged-in User
    public static JwtUserPrincipal getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new AccessDeniedException("No authenticated user found");
        }

        return principal;
    }
}