package com.ecommerce.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.auth.dto.ResetPasswordRequest;
import com.ecommerce.auth.event.UserSecurityVersionChangedApplicationEvent;
import com.ecommerce.auth.event.UserSecurityVersionChangedEvent;
import com.ecommerce.auth.exception.InvalidPasswordResetTokenException;
import com.ecommerce.auth.exception.PasswordConfirmationMismatchException;
import com.ecommerce.auth.model.PasswordResetToken;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.PasswordResetTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.SecureTokenService;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final SecureTokenService secureTokenService;
    private final PasswordResetEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final long expirationMinutes;
    private final ApplicationEventPublisher eventPublisher;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            SecureTokenService secureTokenService,
            PasswordResetEmailService emailService,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            @Value(
                    "${application.password-reset.expiration-minutes}"
            ) long expirationMinutes
    ) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.secureTokenService = secureTokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.expirationMinutes = expirationMinutes;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail
                = normalizeEmail(email);

        Optional<User> optionalUser
                = userRepository.findByEmailIgnoreCase(
                        normalizedEmail
                );

        /*
         * Return normally even when the email does not exist.
         * The controller will return the same generic response.
         */
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();
        Instant now = Instant.now();

        /*
         * Invalidate previously issued, unused links.
         */
        tokenRepository.revokeActiveTokensForUser(
                user.getId(),
                now
        );

        String rawToken
                = secureTokenService.generateToken();

        String tokenHash
                = secureTokenService.hashToken(rawToken);

        PasswordResetToken resetToken
                = PasswordResetToken.builder()
                        .tokenHash(tokenHash)
                        .user(user)
                        .expiresAt(
                                now.plus(
                                        expirationMinutes,
                                        ChronoUnit.MINUTES
                                )
                        )
                        .build();

        /*
         * saveAndFlush ensures the token is persisted before
         * attempting to send its email.
         */
        tokenRepository.saveAndFlush(resetToken);

        emailService.sendPasswordResetEmail(
                user,
                rawToken
        );
    }

    @Transactional
    public void resetPassword(
            ResetPasswordRequest request) {

        validatePasswordConfirmation(request);

        String tokenHash
                = secureTokenService.hashToken(
                        request.token().trim()
                );

        PasswordResetToken resetToken
                = tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(
                                InvalidPasswordResetTokenException::new
                        );

        if (!resetToken.isValid()) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = resetToken.getUser();

        /*
         * Do not allow resetting to the current password.
         */
        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword())) {

            throw new PasswordConfirmationMismatchException(
                    "New password must be different "
                    + "from the current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        user.setSecurityVersion(
                user.getSecurityVersion() + 1
        );

        resetToken.markUsed();

        User savedUser
                = userRepository.save(user);

        tokenRepository.save(resetToken);

        tokenRepository.revokeActiveTokensForUser(
                user.getId(),
                Instant.now()
        );

        UserSecurityVersionChangedEvent securityEvent
                = new UserSecurityVersionChangedEvent(
                        UUID.randomUUID(),
                        "UserSecurityVersionChanged",
                        1,
                        savedUser.getId(),
                        savedUser.getSecurityVersion(),
                        Instant.now()
                );

        eventPublisher.publishEvent(
                new UserSecurityVersionChangedApplicationEvent(
                        securityEvent
                )
        );

        /*
         * Revoke any other active reset links belonging
         * to the same account.
         */
        tokenRepository.revokeActiveTokensForUser(
                user.getId(),
                Instant.now()
        );
    }

    private void validatePasswordConfirmation(
            ResetPasswordRequest request) {

        if (!request.newPassword()
                .equals(request.confirmPassword())) {

            throw new PasswordConfirmationMismatchException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }
}
