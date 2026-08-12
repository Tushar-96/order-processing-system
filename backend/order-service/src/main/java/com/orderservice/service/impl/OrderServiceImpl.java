package com.orderservice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.response.OrderResponse;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.event.OrderCancelledApplicationEvent;
import com.orderservice.event.OrderCancelledEvent;
import com.orderservice.event.OrderCreatedApplicationEvent;
import com.orderservice.event.OrderCreatedEvent;
import com.orderservice.exception.OrderActionNotAllowedException;
import com.orderservice.exception.ResourceNotFoundException;
import com.orderservice.mapper.OrderMapper;
import com.orderservice.repository.OrderRepository;
import com.orderservice.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public OrderResponse createOrder(
            Long userId,
            OrderRequest request
    ) {
        Order order = OrderMapper.toEntity(userId, request);

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent kafkaEvent
                = OrderMapper.toOrderCreatedEvent(
                        savedOrder,
                        request
                );

        applicationEventPublisher.publishEvent(
                new OrderCreatedApplicationEvent(kafkaEvent)
        );

        return OrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long userId,
            Long orderId) {

        Order order = findOwnedOrder(userId, orderId);
        return OrderMapper.toResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(
            Long userId,
            Long orderId) {

        Order order = findOwnedOrder(userId, orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderActionNotAllowedException(
                    "Order is already cancelled"
            );
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            throw new OrderActionNotAllowedException(
                    "Order cannot be cancelled while inventory "
                    + "verification is pending"
            );
        }

        if (order.getStatus()
                == OrderStatus.INVENTORY_REJECTED) {

            throw new OrderActionNotAllowedException(
                    "Order was rejected by inventory"
                    + rejectionDetails(order)
            );
        }

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new OrderActionNotAllowedException(
                    "A shipped order cannot be cancelled"
            );
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderActionNotAllowedException(
                    "A delivered order cannot be cancelled"
            );
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderActionNotAllowedException(
                    "Only a confirmed order can be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        OrderCancelledEvent kafkaEvent
                = new OrderCancelledEvent(
                        UUID.randomUUID(),
                        "OrderCancelled",
                        1,
                        savedOrder.getId(),
                        savedOrder.getCustomerId(),
                        Instant.now()
                );

        applicationEventPublisher.publishEvent(
                new OrderCancelledApplicationEvent(kafkaEvent)
        );

        return OrderMapper.toResponse(savedOrder);
    }

    private Order findOwnedOrder(
            Long userId,
            Long orderId) {

        return orderRepository
                .findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "Order not found"
        ));
    }

    private String rejectionDetails(Order order) {
        if (order.getInventoryRejectionReason() == null) {
            return "";
        }

        return ": " + order.getInventoryRejectionReason();
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
