package com.ecommerce.auth.dto;

import java.util.List;

public record CurrentUserResponse(
        String email,
        List<String> authorities
        ) {

}
