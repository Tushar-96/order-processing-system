package com.orderservice.event;

public record OrderItemRequested(
        Long productId,
        int quantity
        ) {

}
