package com.ecommerce.auth.kafka;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.ecommerce.auth.event.UserSecurityVersionChangedEvent;

@Component
public class UserSecurityVersionChangedProducer {

    private final KafkaTemplate<
            String, UserSecurityVersionChangedEvent> kafkaTemplate;

    private final String topic;

    public UserSecurityVersionChangedProducer(
            KafkaTemplate<
                    String, UserSecurityVersionChangedEvent> kafkaTemplate,
            @Value(
                    "${application.kafka.topics."
                    + "user-security-version-changed}"
            ) String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(
            UserSecurityVersionChangedEvent event
    ) {
        try {
            kafkaTemplate.send(
                    topic,
                    event.userId().toString(),
                    event
            ).get(10, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while publishing "
                    + "security-version event",
                    exception
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to publish security-version "
                    + "event for user " + event.userId(),
                    exception
            );
        }
    }
}
