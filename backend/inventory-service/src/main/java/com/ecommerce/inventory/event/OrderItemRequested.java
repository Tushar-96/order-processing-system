package com.ecommerce.inventory.event;

public record OrderItemRequested(
        Long productId,
        int quantity
        ) {

}
