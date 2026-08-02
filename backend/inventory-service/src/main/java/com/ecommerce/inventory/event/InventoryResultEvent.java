package com.ecommerce.inventory.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryResultEvent(
        UUID eventId,
        String eventType,
        int version,
        UUID causationId,
        Long orderId,
        InventoryResultStatus status,
        InventoryRejectionReason rejectionReason,
        String message,
        List<ReservedInventoryItem> items,
        BigDecimal totalAmount,
        Instant occurredAt
        ) {

}
