package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.dtos.OrderEvent;
import com.ecommerce.orderservice.dtos.OrderResponse;
import com.ecommerce.orderservice.models.OrderStatus;
import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(String token, Long userId);

    boolean updateOrderStatus(Long orderId, OrderStatus status);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByUser(Long userId);

    OrderResponse cancelOrder(Long orderId);

    void restoreStock(Long productId, int quantity);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    String getOrderStatus(Long orderId);

    OrderEvent getOrderEventById(Long orderId);

}
