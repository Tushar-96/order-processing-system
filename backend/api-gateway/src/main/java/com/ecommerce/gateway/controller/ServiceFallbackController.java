package com.ecommerce.gateway.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.gateway.dto.ServiceUnavailableResponse;

@RestController
@RequestMapping("/fallback")
public class ServiceFallbackController {

    @RequestMapping("/order")
    public ResponseEntity<ServiceUnavailableResponse> orderFallback() {
        return unavailable(
                "order-service",
                "Order Service is temporarily unavailable"
        );
    }

    @RequestMapping("/inventory")
    public ResponseEntity<ServiceUnavailableResponse> inventoryFallback() {
        return unavailable(
                "inventory-service",
                "Inventory Service is temporarily unavailable"
        );
    }

    private ResponseEntity<ServiceUnavailableResponse> unavailable(
            String service,
            String message) {

        ServiceUnavailableResponse response
                = new ServiceUnavailableResponse(
                        Instant.now(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "SERVICE_UNAVAILABLE",
                        message,
                        service
                );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
