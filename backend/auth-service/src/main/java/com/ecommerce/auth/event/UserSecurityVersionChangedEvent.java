package com.ecommerce.auth.event;

import java.time.Instant;
import java.util.UUID;

public record UserSecurityVersionChangedEvent(
        UUID eventId,
        String eventType,
        int version,
        Long userId,
        long securityVersion,
        Instant occurredAt
        ) {

}
