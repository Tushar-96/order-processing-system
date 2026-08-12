package com.orderservice.kafka;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.orderservice.event.OrderCreatedApplicationEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final OrderCreatedEventProducer eventProducer;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            OrderCreatedApplicationEvent applicationEvent
    ) {
        eventProducer.publish(applicationEvent.event());
    }
}
