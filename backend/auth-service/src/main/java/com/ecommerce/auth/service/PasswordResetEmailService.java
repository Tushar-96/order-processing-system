package com.ecommerce.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecommerce.auth.model.User;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromEmail;
    private final long expirationMinutes;

    public PasswordResetEmailService(
            JavaMailSender mailSender,
            @Value("${application.password-reset.frontend-url}") String frontendUrl,
            @Value("${application.password-reset.from-email}") String fromEmail,
            @Value(
                    "${application.password-reset.expiration-minutes}"
            ) long expirationMinutes) {

        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromEmail = fromEmail;
        this.expirationMinutes = expirationMinutes;
    }

    public void sendPasswordResetEmail(
            User user,
            String rawToken) {

        String resetUrl
                = UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .path("/reset-password")
                        .queryParam("token", rawToken)
                        .build()
                        .toUriString();

        SimpleMailMessage message
                = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject(
                "Reset your OrderFlow password"
        );

        message.setText(
                buildEmailBody(
                        user.getFullName(),
                        resetUrl
                )
        );

        mailSender.send(message);
    }

    private String buildEmailBody(
            String fullName,
            String resetUrl) {

        return """
                Hello %s,

                We received a request to reset your OrderFlow password.

                Use the link below to choose a new password:

                %s

                This link expires in %d minutes and can only be used once.

                If you did not request a password reset, you can ignore this email.

                OrderFlow Team
                """.formatted(
                fullName,
                resetUrl,
                expirationMinutes
        );
    }
}
