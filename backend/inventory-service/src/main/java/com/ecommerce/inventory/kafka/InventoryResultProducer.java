package com.ecommerce.inventory.kafka;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ecommerce.inventory.event.InventoryResultEvent;
import com.ecommerce.inventory.exception.KafkaPublishException;

@Component
public class InventoryResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String inventoryResultTopic;

    public InventoryResultProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${application.kafka.topics.inventory-result}") String inventoryResultTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.inventoryResultTopic = inventoryResultTopic;
    }

    public void publish(InventoryResultEvent event) {
        try {
            kafkaTemplate.send(
                    inventoryResultTopic,
                    event.orderId().toString(),
                    event
            ).get(10, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new KafkaPublishException(
                    "Interrupted while publishing inventory result",
                    exception
            );

        } catch (Exception exception) {
            throw new KafkaPublishException(
                    "Failed to publish inventory result for order "
                    + event.orderId(),
                    exception
            );
        }
    }
}
