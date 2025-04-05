package com.ecommerce.orderservice.controllers;

import com.ecommerce.orderservice.dtos.OrderEvent;
import com.ecommerce.orderservice.dtos.OrderRequest;
import com.ecommerce.orderservice.dtos.OrderResponse;
import com.ecommerce.orderservice.services.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void testPlaceOrder() {
        String token = "Bearer token";
        OrderRequest orderRequest = new OrderRequest(1L); // Assuming OrderRequest holds a userId.
        OrderResponse expectedResponse = new OrderResponse(1L, "PLACED", new BigDecimal("100.00"), "paymentLink");

        when(orderService.placeOrder(token, orderRequest.getUserId())).thenReturn(expectedResponse);

        ResponseEntity<OrderResponse> response = orderController.placeOrder(token, orderRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testGetOrderById() {
        OrderResponse expectedResponse = new OrderResponse(1L, "PLACED", new BigDecimal("100.00"), "paymentLink");
        when(orderService.getOrderById(1L)).thenReturn(expectedResponse);

        ResponseEntity<OrderResponse> response = orderController.getOrderById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testGetOrdersByUser() {
        List<OrderResponse> expectedList = List.of(new OrderResponse(1L, "PLACED", new BigDecimal("100.00"), "paymentLink"));
        when(orderService.getOrdersByUser(1L)).thenReturn(expectedList);

        ResponseEntity<List<OrderResponse>> response = orderController.getOrdersByUser(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedList, response.getBody());
    }

    @Test
    void testCancelOrder() {
        ResponseEntity<Void> response = orderController.cancelOrder(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetOrderStatus() {
        OrderResponse expectedResponse = new OrderResponse(1L, "SHIPPED", new BigDecimal("100.00"), "paymentLink");
        when(orderService.getOrderById(1L)).thenReturn(expectedResponse);

        ResponseEntity<String> response = orderController.getOrderStatus(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SHIPPED", response.getBody());
    }

    @Test
    void testGetOrderEvent() {
        OrderEvent expectedEvent = new OrderEvent(1L, 1L, 0d, null, "CREATED", LocalDateTime.now());
        when(orderService.getOrderEventById(1L)).thenReturn(expectedEvent);

        ResponseEntity<OrderEvent> response = orderController.getOrderEvent(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedEvent, response.getBody());
    }
}
