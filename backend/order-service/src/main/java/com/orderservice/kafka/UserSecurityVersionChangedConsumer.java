package com.orderservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.orderservice.event.UserSecurityVersionChangedEvent;
import com.orderservice.service.UserSecurityVersionService;

@Component
public class UserSecurityVersionChangedConsumer {

    private final UserSecurityVersionService service;

    public UserSecurityVersionChangedConsumer(
            UserSecurityVersionService service) {

        this.service = service;
    }

    @KafkaListener(
            topics
            = "${application.kafka.topics.user-security-version-changed}",
            groupId = "order-user-security",
            containerFactory
            = "userSecurityKafkaListenerContainerFactory"
    )
    public void consume(
            UserSecurityVersionChangedEvent event) {

        service.process(event);
    }
}
