package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.dtos.OrderEvent;
import com.ecommerce.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderRepository orderRepository;
    private static final String ORDER_TOPIC = "order-events";

    // Track orders that need to be auto-canceled
    private final Map<Long, LocalDateTime> pendingOrders = new ConcurrentHashMap<>();

    public void sendOrderEvent(OrderEvent orderEvent) {
        System.out.println("Sending OrderEvent: " + orderEvent);
        kafkaTemplate.send(ORDER_TOPIC, orderEvent);

        // Track pending orders for auto-cancellation
        if ("PENDING".equals(orderEvent.getStatus())) {
            pendingOrders.put(orderEvent.getOrderId(), orderEvent.getExpiresAt());
        }
    }
}
