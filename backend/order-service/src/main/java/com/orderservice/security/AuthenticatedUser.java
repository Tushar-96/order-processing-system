package com.orderservice.security;

public record AuthenticatedUser(
        Long userId,
        String email,
        String role
        ) {

}
