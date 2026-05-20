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
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.security.auth.Login;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public void signup(AuthRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        validatePassword(request.getPassword());     //Password must have: 1 uppercase letter , 1 digit , 1 special character , minimum 8 length

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(AuthRequest request) {

        User user = (User) userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = UUID.randomUUID().toString(); //generate Refresh Token

        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(1000L *60*60*24*30*6)) //6months
                .revoked(false)
                .build();

        //Save Refresh Token in DB
        refreshTokenRepository.save(token);

        return new LoginResponse(user.getId() ,accessToken, refreshToken);
    }

    //for Registration
    //a password validation method using a regex (regular expression)
    //Password must have: 1 uppercase letter , 1 digit , 1 special character , minimum 8 length
    private void validatePassword(String password) {
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            throw new InvalidCredentialsException("Weak password: Password must be at least 8 characters long, include one uppercase letter, one number, and one special character.");
        }
    }

    //Refresh token = permission to get NEW access token
    public String refreshToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid refresh token"));

        // revoked check
        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token revoked or expired");
        }

        //if refresh token is validated then only generate new access token
        //  generate NEW access token
        return jwtService.generateToken(token.getUser());
    }
}