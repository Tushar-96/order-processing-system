package com.orderservice.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    // @NotNull(message = "customerId is required")
    // private Long customerId;
    @NotNull(message = "totalAmount is required")
    @Positive(message = "totalAmount must be greater than 0")
    private BigDecimal totalAmount;
}
