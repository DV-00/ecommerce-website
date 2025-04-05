package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.dtos.OrderEvent;
import com.ecommerce.orderservice.dtos.OrderItemDto;
import com.ecommerce.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderProducer orderProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendOrderEvent_Success() {
        // Given
        OrderEvent orderEvent = new OrderEvent(
                1L, 101L, 250.50,
                Collections.singletonList(new OrderItemDto(10L, 2)),
                "PENDING", LocalDateTime.now().plusMinutes(30)
        );

        orderProducer.sendOrderEvent(orderEvent);

        verify(kafkaTemplate, times(1)).send("order-events", orderEvent);  // ✅ Verify Kafka send
    }

    @Test
    void testSendOrderEvent_TracksPendingOrders() {
        OrderEvent orderEvent = new OrderEvent(
                2L, 102L, 100.00,
                Collections.singletonList(new OrderItemDto(11L, 1)),
                "PENDING", LocalDateTime.now().plusMinutes(15)
        );

        orderProducer.sendOrderEvent(orderEvent);

        Map<Long, LocalDateTime> pendingOrders = getPendingOrders();
        assertTrue(pendingOrders.containsKey(2L));
    }

    @Test
    void testSendOrderEvent_DoesNotTrackNonPendingOrders() {
        OrderEvent orderEvent = new OrderEvent(
                3L, 103L, 200.00,
                Collections.singletonList(new OrderItemDto(12L, 2)),
                "COMPLETED", LocalDateTime.now()
        );

        orderProducer.sendOrderEvent(orderEvent);

        Map<Long, LocalDateTime> pendingOrders = getPendingOrders();
        assertFalse(pendingOrders.containsKey(3L));
    }

    @SuppressWarnings("unchecked")
    private Map<Long, LocalDateTime> getPendingOrders() {
        try {
            var field = OrderProducer.class.getDeclaredField("pendingOrders");
            field.setAccessible(true);
            return (Map<Long, LocalDateTime>) field.get(orderProducer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access pendingOrders", e);
        }
    }
}
