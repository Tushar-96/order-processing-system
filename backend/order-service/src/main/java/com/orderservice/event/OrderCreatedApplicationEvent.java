package com.orderservice.event;

public record OrderCreatedApplicationEvent(
        OrderCreatedEvent event
        ) {

}
