package com.orderservice.service;

import java.util.List;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(Long userId, OrderRequest request);

    List<OrderResponse> getOrdersByUserId(Long userId);

    OrderResponse getOrderById(Long userId, Long orderId);

    OrderResponse cancelOrder(Long userId, Long orderId);

    List<OrderResponse> getAllOrders();
}
