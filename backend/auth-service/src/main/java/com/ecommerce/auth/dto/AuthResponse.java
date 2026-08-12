package com.ecommerce.auth.dto;

import com.ecommerce.auth.model.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        String fullName,
        Role role
        ) {

}
