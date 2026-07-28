package com.orderservice.service;

import java.util.List;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();
}