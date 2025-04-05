package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.dtos.OrderEvent;
import com.ecommerce.paymentservice.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import static org.mockito.Mockito.*;

public class PaymentConsumerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentConsumer paymentConsumer;

    private static final Logger logger = LoggerFactory.getLogger(PaymentConsumer.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessOrder_WithNullItems() {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setItems(null);

        paymentConsumer.processOrder(orderEvent);

        verify(paymentService, never()).processPayment(any(OrderEvent.class));
        assert orderEvent.getItems() != null;
        assert orderEvent.getItems().isEmpty();
    }

    @Test
    public void testProcessOrder_WithItems() {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setItems(new ArrayList<>());

        paymentConsumer.processOrder(orderEvent);

        verify(paymentService, never()).processPayment(any(OrderEvent.class));
        assert orderEvent.getItems() != null;
        assert orderEvent.getItems().isEmpty();
    }
}