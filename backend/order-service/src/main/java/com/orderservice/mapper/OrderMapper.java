package com.orderservice.mapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.response.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.event.OrderCreatedEvent;
import com.orderservice.event.OrderItemRequested;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(
            Long userId,
            OrderRequest request
    ) {
        return Order.builder()
                .customerId(userId)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .inventoryRejectionReason(
                        order.getInventoryRejectionReason()
                )
                .inventoryMessage(order.getInventoryMessage())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderCreatedEvent toOrderCreatedEvent(
            Order order,
            OrderRequest request
    ) {
        List<OrderItemRequested> items = request.getItems()
                .stream()
                .map(item -> new OrderItemRequested(
                item.productId(),
                item.quantity()
        ))
                .toList();

        return new OrderCreatedEvent(
                UUID.randomUUID(),
                "OrderCreated",
                1,
                order.getId(),
                order.getCustomerId(),
                items,
                Instant.now()
        );
    }

}
