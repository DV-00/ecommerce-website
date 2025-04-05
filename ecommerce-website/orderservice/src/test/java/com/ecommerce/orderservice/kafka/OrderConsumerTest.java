package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.dtos.PaymentEvent;
import com.ecommerce.orderservice.dtos.CartClearEvent;
import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderItem;
import com.ecommerce.orderservice.models.OrderStatus;
import com.ecommerce.orderservice.repositories.OrderRepository;
import com.ecommerce.orderservice.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import com.ecommerce.orderservice.models.PaymentStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, CartClearEvent> cartKafkaTemplate;

    @InjectMocks
    private OrderConsumer orderConsumer;

    private PaymentEvent successPaymentEvent;
    private PaymentEvent failedPaymentEvent;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        successPaymentEvent = new PaymentEvent(1L, 101L, "PAY123", PaymentStatusEnum.SUCCESS, LocalDateTime.now());
        failedPaymentEvent = new PaymentEvent(2L, 102L, "PAY456", PaymentStatusEnum.FAILED, LocalDateTime.now());

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(100L);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setItems(List.of(
                new OrderItem(1L, 10L, 2, 50.00, testOrder, false)
        ));
    }

    @Test
    void testProcessPaymentStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderConsumer.processPaymentStatus(successPaymentEvent);

        verify(orderService, times(1)).updateOrderStatus(1L, OrderStatus.COMPLETED);
        verify(cartKafkaTemplate, times(1)).send(eq("cart-events"), any(CartClearEvent.class));
    }

    @Test
    void testProcessPaymentStatus_Failed() {
        when(orderRepository.findById(2L)).thenReturn(Optional.of(testOrder));

        orderConsumer.processPaymentStatus(failedPaymentEvent);

        verify(orderService, times(1)).updateOrderStatus(2L, OrderStatus.CANCELED);
        verify(orderService, times(1)).restoreStock(10L, 2);
    }

    @Test
    void testProcessPaymentStatus_InvalidEvent() {
        orderConsumer.processPaymentStatus(null);
        verify(orderService, never()).updateOrderStatus(anyLong(), any());
    }

    @Test
    void testProcessPaymentStatus_UnknownStatus() {
        PaymentEvent unknownEvent = new PaymentEvent(
                3L,
                null,
                null,
                null,
                null
        );

        orderConsumer.processPaymentStatus(unknownEvent);
        verify(orderService, never()).updateOrderStatus(anyLong(), any());
    }

    @Test
    void testProcessPaymentStatus_OrderNotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Expect exception
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            orderConsumer.processPaymentStatus(successPaymentEvent);
        });

        assertEquals("Order not found!", thrownException.getMessage());

        verify(orderService, times(1)).updateOrderStatus(anyLong(), any());

        verify(cartKafkaTemplate, never()).send(any(), any());
    }


}
