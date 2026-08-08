package com.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,
        @Positive(message = "Quantity must be greater than zero")
        int quantity
        ) {

}
