package com.orderservice.exception;

public class OrderActionNotAllowedException extends RuntimeException {

    public OrderActionNotAllowedException(String message) {
        super(message);
    }
}