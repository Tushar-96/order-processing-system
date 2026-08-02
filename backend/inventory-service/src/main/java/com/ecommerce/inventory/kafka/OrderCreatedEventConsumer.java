package com.ecommerce.inventory.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ecommerce.inventory.event.InventoryResultEvent;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.service.InventoryReservationService;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(
                    OrderCreatedEventConsumer.class
            );

    private final InventoryReservationService reservationService;
    private final InventoryResultProducer resultProducer;

    public OrderCreatedEventConsumer(
            InventoryReservationService reservationService,
            InventoryResultProducer resultProducer) {

        this.reservationService = reservationService;
        this.resultProducer = resultProducer;
    }

    @KafkaListener(
            topics = "${application.kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(OrderCreatedEvent event) {
        LOGGER.info(
                "Received OrderCreatedEvent eventId={} orderId={}",
                event.eventId(),
                event.orderId()
        );

        InventoryResultEvent result
                = reservationService.process(event);

        /*
         * process() is on a separate Spring bean. Its database
         * transaction commits before it returns.
         */
        resultProducer.publish(result);

        LOGGER.info(
                "Published InventoryResultEvent eventId={} "
                + "orderId={} status={}",
                result.eventId(),
                result.orderId(),
                result.status()
        );
    }
}
