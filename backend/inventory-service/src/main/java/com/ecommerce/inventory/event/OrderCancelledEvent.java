package com.ecommerce.inventory.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        String eventType,
        int version,
        Long orderId,
        Long userId,
        Instant occurredAt
        ) {

}
