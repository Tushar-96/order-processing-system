package com.ecommerce.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.auth.model.PasswordResetToken;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
            update PasswordResetToken token
            set token.revokedAt = :revokedAt
            where token.user.id = :userId
              and token.usedAt is null
              and token.revokedAt is null
              and token.expiresAt > :revokedAt
            """)
    int revokeActiveTokensForUser(
            @Param("userId") Long userId,
            @Param("revokedAt") Instant revokedAt
    );

    @Modifying
    @Query("""
            delete from PasswordResetToken token
            where token.expiresAt < :cutoff
               or token.usedAt < :cutoff
               or token.revokedAt < :cutoff
            """)
    int deleteOldTokens(
            @Param("cutoff") Instant cutoff
    );
}
