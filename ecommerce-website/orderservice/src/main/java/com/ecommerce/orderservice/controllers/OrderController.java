package com.ecommerce.orderservice.controllers;

import com.ecommerce.orderservice.dtos.OrderEvent;
import com.ecommerce.orderservice.dtos.OrderRequest;
import com.ecommerce.orderservice.dtos.OrderResponse;
import com.ecommerce.orderservice.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.placeOrder(token, orderRequest.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<String> getOrderStatus(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response.getStatus());
    }

    @GetMapping("/event/{orderId}")
    public ResponseEntity<OrderEvent> getOrderEvent(@PathVariable Long orderId) {
        OrderEvent orderEvent = orderService.getOrderEventById(orderId);
        return ResponseEntity.ok(orderEvent);
    }
}
