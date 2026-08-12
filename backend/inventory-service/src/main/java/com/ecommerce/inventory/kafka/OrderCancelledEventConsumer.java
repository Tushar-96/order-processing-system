package com.ecommerce.inventory.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.service.InventoryReservationService;

@Component
public class OrderCancelledEventConsumer {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(
                    OrderCancelledEventConsumer.class
            );

    private final InventoryReservationService reservationService;

    public OrderCancelledEventConsumer(
            InventoryReservationService reservationService) {

        this.reservationService = reservationService;
    }

    @KafkaListener(
            topics
            = "${application.kafka.topics.order-cancelled}",
            groupId = "inventory-cancellation-service",
            containerFactory
            = "orderCancelledKafkaListenerContainerFactory"
    )
    public void consume(OrderCancelledEvent event) {
        LOGGER.info(
                "Received OrderCancelledEvent "
                + "eventId={} orderId={}",
                event.eventId(),
                event.orderId()
        );

        reservationService.release(event);

        LOGGER.info(
                "Released inventory for orderId={}",
                event.orderId()
        );
    }
}
