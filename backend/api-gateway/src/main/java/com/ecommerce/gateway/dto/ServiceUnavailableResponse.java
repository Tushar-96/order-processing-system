package com.ecommerce.gateway.dto;

import java.time.Instant;

public record ServiceUnavailableResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String service
        ) {

}
