package com.orderservice.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        String eventType,
        int version,
        Long orderId,
        Long userId,
        List<OrderItemRequested> items,
        Instant occurredAt
        ) {

}
