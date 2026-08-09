package com.orderservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.orderservice.event.InventoryResultEvent;
import com.orderservice.service.InventoryResultService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryResultEventConsumer {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(
                    InventoryResultEventConsumer.class
            );

    private final InventoryResultService inventoryResultService;

    @KafkaListener(
            topics = "${application.kafka.topics.inventory-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(InventoryResultEvent event) {
        LOGGER.info(
                "Received InventoryResultEvent "
                + "eventId={} orderId={} status={}",
                event.eventId(),
                event.orderId(),
                event.status()
        );

        inventoryResultService.process(event);

        LOGGER.info(
                "Updated order {} from inventory result {}",
                event.orderId(),
                event.eventId()
        );
    }
}
