package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.AuthRequest;
import com.priyansu.workflow.dto.AuthResponse;
import com.priyansu.workflow.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {

        String token = authService.login(request);

        return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
    }
}