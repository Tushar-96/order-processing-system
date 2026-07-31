package com.ecommerce.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.01",
                message = "Price must be at least 0.01"
        )
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Price must have at most 17 integer digits and 2 decimal places"
        )
        BigDecimal price,

        @PositiveOrZero(message = "Available quantity cannot be negative")
        int availableQuantity

) {
}