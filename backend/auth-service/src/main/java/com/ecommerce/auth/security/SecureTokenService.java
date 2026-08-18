package com.ecommerce.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

@Service
public class SecureTokenService {

    private static final int TOKEN_SIZE_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final SecureRandom secureRandom
            = new SecureRandom();

    public String generateToken() {
        byte[] tokenBytes
                = new byte[TOKEN_SIZE_BYTES];

        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Reset token is required"
            );
        }

        try {
            MessageDigest messageDigest
                    = MessageDigest.getInstance(
                            HASH_ALGORITHM
                    );

            byte[] tokenHash
                    = messageDigest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(tokenHash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }
}
