package com.orderservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orderservice.dto.request.OrderRequest;
import com.orderservice.dto.response.OrderResponse;
import com.orderservice.security.AuthenticatedUser;
import com.orderservice.service.OrderService;
import com.orderservice.util.AppConstants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(AppConstants.ORDERS_API)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> getCurrentUserOrders(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                orderService.getOrdersByUserId(user.userId())
        );
    }

    @PostMapping()
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(
                        user.userId(),
                        request
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrderById(user.userId(), id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.cancelOrder(user.userId(), id)
        );
    }
}
