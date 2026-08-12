package com.ecommerce.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(

        Long id,
        String name,
        String description,
        BigDecimal price,
        int availableQuantity,
        Long version,
        Instant createdAt,
        Instant updatedAt

) {
}