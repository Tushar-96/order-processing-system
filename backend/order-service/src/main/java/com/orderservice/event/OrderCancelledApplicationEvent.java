package com.orderservice.event;

public record OrderCancelledApplicationEvent(
        OrderCancelledEvent event
        ) {

}
