package com.orderservice.kafka;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.orderservice.event.OrderCancelledEvent;
import com.orderservice.exception.KafkaPublishException;

@Component
public class OrderCancelledEventProducer {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(
                    OrderCancelledEventProducer.class
            );

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderCancelledTopic;

    public OrderCancelledEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${application.kafka.topics.order-cancelled}") String orderCancelledTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.orderCancelledTopic = orderCancelledTopic;
    }

    public void publish(OrderCancelledEvent event) {
        try {
            LOGGER.info(
                    "Publishing OrderCancelledEvent "
                    + "eventId={} orderId={} topic={}",
                    event.eventId(),
                    event.orderId(),
                    orderCancelledTopic
            );

            kafkaTemplate.send(
                    orderCancelledTopic,
                    event.orderId().toString(),
                    event
            ).get(10, TimeUnit.SECONDS);

            LOGGER.info(
                    "Published OrderCancelledEvent "
                    + "eventId={} orderId={}",
                    event.eventId(),
                    event.orderId()
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new KafkaPublishException(
                    "Interrupted while publishing cancellation "
                    + "for order " + event.orderId(),
                    exception
            );
        } catch (Exception exception) {
            LOGGER.error(
                    "Failed to publish cancellation for orderId={}",
                    event.orderId(),
                    exception
            );

            throw new KafkaPublishException(
                    "Failed to publish cancellation for order "
                    + event.orderId(),
                    exception
            );
        }
    }
}
