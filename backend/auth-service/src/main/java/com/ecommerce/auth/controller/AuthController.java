package com.ecommerce.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.CurrentUserResponse;
import com.ecommerce.auth.dto.ForgotPasswordRequest;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.MessageResponse;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.ResetPasswordRequest;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthService authService,
            PasswordResetService passwordResetService) {

        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> currentUser(
            Authentication authentication) {

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        CurrentUserResponse response = new CurrentUserResponse(
                authentication.getName(),
                authorities
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestPasswordReset(
                request.email()
        );

        MessageResponse response
                = new MessageResponse(
                        "If an account exists for that email, "
                        + "a password-reset link has been sent."
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);

        MessageResponse response
                = new MessageResponse(
                        "Your password has been reset successfully."
                );

        return ResponseEntity.ok(response);
    }
}
