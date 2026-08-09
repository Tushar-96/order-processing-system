package com.orderservice.kafka;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.orderservice.event.OrderCreatedEvent;
import com.orderservice.exception.KafkaPublishException;

@Component
public class OrderCreatedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderCreatedTopic;

    public OrderCreatedEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${application.kafka.topics.order-created}") String orderCreatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    public void publish(OrderCreatedEvent event) {
        try {
            kafkaTemplate.send(
                    orderCreatedTopic,
                    event.orderId().toString(),
                    event
            ).get(10, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new KafkaPublishException(
                    "Interrupted while publishing order-created event",
                    exception
            );

        } catch (Exception exception) {
            throw new KafkaPublishException(
                    "Failed to publish event for order "
                    + event.orderId(),
                    exception
            );
        }
    }
}
