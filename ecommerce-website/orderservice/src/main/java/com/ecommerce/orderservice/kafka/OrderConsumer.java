package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.dtos.PaymentEvent;
import com.ecommerce.orderservice.dtos.CartClearEvent;
import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderStatus;
import com.ecommerce.orderservice.services.OrderService;
import com.ecommerce.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, CartClearEvent> cartKafkaTemplate;

    // Process Payment Status
    @KafkaListener(topics = "payment-events", groupId = "my-group")
    public void processPaymentStatus(PaymentEvent paymentEvent) {

        logger.info("Received PaymentEvent: {}", paymentEvent);
        if (paymentEvent == null || paymentEvent.getOrderId() == null || paymentEvent.getStatus() == null) {
            logger.warn("Received invalid PaymentEvent: {}", paymentEvent);
            return;
        }

        // Map PaymentEvent status to OrderStatus
        OrderStatus orderStatus;
        String paymentStatusStr = paymentEvent.getStatus().name();
        if (paymentStatusStr.equalsIgnoreCase("SUCCESS")) {
            orderStatus = OrderStatus.COMPLETED;
        } else if (paymentStatusStr.equalsIgnoreCase("FAILED")) {
            orderStatus = OrderStatus.CANCELED;
        } else if (paymentStatusStr.equalsIgnoreCase("PENDING")) {
            orderStatus = OrderStatus.PENDING;
        } else if (paymentStatusStr.equalsIgnoreCase("CANCELED")) {
            orderStatus = OrderStatus.CANCELED;
        } else if (paymentStatusStr.equalsIgnoreCase("REFUNDED")) {
            orderStatus = OrderStatus.REFUNDED;
        } else {
            logger.error("Unknown PaymentEvent status: {}", paymentStatusStr);
            return;
        }

        // Update order status
        orderService.updateOrderStatus(paymentEvent.getOrderId(), orderStatus);
        logger.info("Order ID {} updated to status: {}", paymentEvent.getOrderId(), orderStatus);

        // If order is completed, clear cart
        if (orderStatus == OrderStatus.COMPLETED) {
            Order order = orderRepository.findById(paymentEvent.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found!"));
            CartClearEvent cartClearEvent = new CartClearEvent(order.getUserId());
            cartKafkaTemplate.send("cart-events", cartClearEvent);
            logger.info("Cart cleared for User ID: {}", order.getUserId());
        }

        // If order is canceled, restore stock
        if (orderStatus == OrderStatus.CANCELED) {
            Order order = orderRepository.findById(paymentEvent.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found!"));

            if (order.getItems() != null) {
                order.getItems().forEach(item -> {
                    orderService.restoreStock(item.getProductId(), item.getQuantity());
                    logger.info("Restored stock for Product ID: {} | Quantity: {}",
                            item.getProductId(), item.getQuantity());
                });
            } else {
                logger.warn("Order {} has no items to restore stock.", order.getId());
            }
        }
    }
}
