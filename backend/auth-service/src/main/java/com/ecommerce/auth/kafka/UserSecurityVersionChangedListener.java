package com.ecommerce.auth.kafka;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ecommerce.auth.event.UserSecurityVersionChangedApplicationEvent;

@Component
public class UserSecurityVersionChangedListener {

    private final UserSecurityVersionChangedProducer producer;

    public UserSecurityVersionChangedListener(
            UserSecurityVersionChangedProducer producer) {

        this.producer = producer;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            UserSecurityVersionChangedApplicationEvent applicationEvent) {

        producer.publish(applicationEvent.event());
    }
}
