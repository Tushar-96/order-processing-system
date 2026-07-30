package com.orderservice.service.impl;

import java.util.List;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.request.OrderUpdateRequest;
import com.orderservice.dto.response.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.exception.OrderActionNotAllowedException;
import com.orderservice.exception.ResourceNotFoundException;
import com.orderservice.mapper.OrderMapper;
import com.orderservice.repository.OrderRepository;
import com.orderservice.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Order order = OrderMapper.toEntity(request);
        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderActionNotAllowedException(
                    "Order cannot be updated because its current status is " + order.getStatus()
            );
        }

        order.setCustomerId(request.getCustomerId());
        order.setTotalAmount(request.getTotalAmount());

        Order updatedOrder = orderRepository.save(order);
        return OrderMapper.toResponse(updatedOrder);
    }

    @Override
    public OrderResponse deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderActionNotAllowedException("Delivered order cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderActionNotAllowedException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        return OrderMapper.toResponse(cancelledOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }
}