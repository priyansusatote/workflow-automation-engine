package com.priyansu.workflow.security;

import com.priyansu.workflow.dto.AuthRequest;
import com.priyansu.workflow.entity.User;
import com.priyansu.workflow.entity.enums.Role;
import com.priyansu.workflow.exception.InvalidCredentialsException;
import com.priyansu.workflow.exception.UserAlreadyExistsException;
import com.priyansu.workflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    public String login(AuthRequest request) {

        User user = (User) userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return jwtService.generateToken(user);
    }

    //for Registration
    //a password validation method using a regex (regular expression)
    //Password must have: 1 uppercase letter , 1 digit , 1 special character , minimum 8 length
    private void validatePassword(String password) {
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            throw new InvalidCredentialsException("Weak password: Password must be at least 8 characters long, include one uppercase letter, one number, and one special character.");
        }
    }
}