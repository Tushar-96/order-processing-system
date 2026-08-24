package com.ecommerce.auth.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
            @Index(
                    name = "idx_password_reset_token_hash",
                    columnList = "token_hash",
                    unique = true
            ),
            @Index(
                    name = "idx_password_reset_user",
                    columnList = "user_id"
            ),
            @Index(
                    name = "idx_password_reset_expires_at",
                    columnList = "expires_at"
            )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Store the SHA-256 hash, not the token sent to the user.
     * A SHA-256 hexadecimal value contains 64 characters.
     */
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64,
            updatable = false
    )
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @Column(
            name = "expires_at",
            nullable = false,
            updatable = false
    )
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isValid() {
        return !isExpired()
                && !isUsed()
                && !isRevoked();
    }

    public void markUsed() {
        usedAt = Instant.now();
    }

    public void revoke() {
        if (!isUsed() && !isRevoked()) {
            revokedAt = Instant.now();
        }
    }
}
