package com.orderservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.orderservice.event.OrderCancelledApplicationEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCancelledEventListener {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(
                    OrderCancelledEventListener.class
            );

    private final OrderCancelledEventProducer eventProducer;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            OrderCancelledApplicationEvent applicationEvent) {

        LOGGER.info(
                "Order cancellation transaction committed "
                + "for orderId={}",
                applicationEvent.event().orderId()
        );

        eventProducer.publish(applicationEvent.event());
    }
}
