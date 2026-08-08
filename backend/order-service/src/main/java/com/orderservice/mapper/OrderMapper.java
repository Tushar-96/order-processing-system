package com.orderservice.mapper;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.response.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(OrderRequest request) {
        return Order.builder()
                // .customerId(request.getCustomerId()) 
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .build();
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
