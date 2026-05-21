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
        cookie.setSecure(false); //"false": for dev(localhost) purpose. {in production(set:true) Cookie only sent over:HTTPS)
        cookie.setPath("/"); //This cookie is available for ALL endpoints in your application
        response.addCookie(cookie);


        return ResponseEntity.ok(Map.of(
                "accessToken", loginResponse.accessToken()));
    }


    //"Refresh tokens allow clients to obtain new short-lived access tokens without forcing the user to log in repeatedly. The frontend automatically calls the refresh endpoint when access tokens expire."
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) { //Read refresh token from cookie

        if (refreshToken == null) {
            throw new InvalidCredentialsException("Refresh token missing");
        }

        String newAccessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }

    //Logout {"Logout should both revoke server-side tokens and clear the refresh token cookie from the browser by returning a Set-Cookie header with Max-Age=0."}
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Invalid authorization header");
        }

        String accessToken = authHeader.substring(7);

        if (refreshToken == null) {
            throw new InvalidCredentialsException("Refresh token missing");
        }
        authService.logout(refreshToken, accessToken);

        //  Clear refresh token cookie from browser (client side)
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //"false": for dev(localhost) purpose. {in production(set:true) Cookie only sent over:HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(0); //tells browser delete cookie imminently
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}