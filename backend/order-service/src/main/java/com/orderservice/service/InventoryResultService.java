package com.orderservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.event.InventoryResultEvent;
import com.orderservice.event.InventoryResultStatus;
import com.orderservice.exception.ResourceNotFoundException;
import com.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryResultService {

    private final OrderRepository orderRepository;

    @Transactional
    public void process(InventoryResultEvent event) {
        validate(event);

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                "Order not found with id: " + event.orderId()
        ));

        // Makes repeated result events harmless.
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        if (event.status() == InventoryResultStatus.RESERVED) {
            if (event.totalAmount() == null
                    || event.totalAmount().signum() <= 0) {
                throw new IllegalArgumentException(
                        "Reserved result requires a positive total amount"
                );
            }

            order.setTotalAmount(event.totalAmount());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setInventoryRejectionReason(null);
            order.setInventoryMessage(event.message());

        } else if (event.status() == InventoryResultStatus.REJECTED) {
            order.setTotalAmount(null);
            order.setStatus(OrderStatus.INVENTORY_REJECTED);

            order.setInventoryRejectionReason(
                    event.rejectionReason() == null
                    ? "PROCESSING_ERROR"
                    : event.rejectionReason().name()
            );

            order.setInventoryMessage(event.message());

        } else {
            throw new IllegalArgumentException(
                    "Unsupported inventory result status: "
                    + event.status()
            );
        }

        orderRepository.save(order);
    }

    private void validate(InventoryResultEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Inventory result event is required"
            );
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException(
                    "Event ID is required"
            );
        }

        if (!"InventoryResult".equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Unsupported event type: "
                    + event.eventType()
            );
        }

        if (event.version() != 1) {
            throw new IllegalArgumentException(
                    "Unsupported event version: "
                    + event.version()
            );
        }

        if (event.orderId() == null
                || event.orderId() <= 0) {
            throw new IllegalArgumentException(
                    "A positive order ID is required"
            );
        }

        if (event.status() == null) {
            throw new IllegalArgumentException(
                    "Inventory result status is required"
            );
        }
    }
}
