package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.AuthRequest;
import com.priyansu.workflow.dto.AuthResponse;
import com.priyansu.workflow.dto.LoginResponse;
import com.priyansu.workflow.exception.InvalidCredentialsException;
import com.priyansu.workflow.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //  SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthRequest request) {

        authService.signup(request);

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully"
        ));
    }

    //  LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request,
                                   HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(request);

        //  Cookie handling here (bcz Refresh token should be in secure Cookie(Http Only))
        Cookie cookie = new Cookie("refreshToken", loginResponse.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);


        return ResponseEntity.ok(Map.of(
                "accessToken", loginResponse.accessToken()));
    }


    //"Refresh tokens allow clients to obtain new short-lived access tokens without forcing the user to log in repeatedly. The frontend automatically calls the refresh endpoint when access tokens expire."
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue("refreshToken") String refreshToken) {

        if (refreshToken == null) {
            throw new InvalidCredentialsException("Refresh token missing");
        }

        String newAccessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }
}