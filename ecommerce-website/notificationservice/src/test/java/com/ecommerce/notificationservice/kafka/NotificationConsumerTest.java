package com.ecommerce.notificationservice.kafka;

import com.ecommerce.notificationservice.dtos.NotificationOrderEvent;
import com.ecommerce.notificationservice.dtos.NotificationPaymentEvent;
import com.ecommerce.notificationservice.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.mockito.Mockito.*;

public class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConsumeOrderEvent_WithNullItems() {
        NotificationOrderEvent orderEvent = new NotificationOrderEvent(1L, 1L, 100.0, null, "CANCELED", LocalDateTime.now());

        notificationConsumer.consumeOrderEvent(orderEvent);

        verify(notificationService).sendNotification(
                eq("1"),
                eq("Order Cancellation"),
                eq("Your order #1 has been canceled due to non-payment.")
        );
    }

    @Test
    public void testConsumeOrderEvent_WithItems() {
        NotificationOrderEvent orderEvent = new NotificationOrderEvent(1L, 1L, 100.0, Collections.emptyList(), "CANCELED", LocalDateTime.now());

        notificationConsumer.consumeOrderEvent(orderEvent);

        verify(notificationService).sendNotification(
                eq("1"),
                eq("Order Cancellation"),
                eq("Your order #1 has been canceled due to non-payment.")
        );
    }

    @Test
    public void testConsumeOrderEvent_IncompleteEvent() {
        NotificationOrderEvent orderEvent = new NotificationOrderEvent(null, 1L, 100.0, Collections.emptyList(), "CANCELED", LocalDateTime.now());

        notificationConsumer.consumeOrderEvent(orderEvent);

        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    public void testConsumePaymentEvent_Success() {
        NotificationPaymentEvent paymentEvent = new NotificationPaymentEvent(1L, 1L, "123", "SUCCESS", LocalDateTime.now());

        notificationConsumer.consumePaymentEvent(paymentEvent);

        verify(notificationService).sendNotification(
                eq("1"),
                eq("Payment Successful"),
                eq("Your payment for order #1 was successful.")
        );
    }

    @Test
    public void testConsumePaymentEvent_Failed() {
        NotificationPaymentEvent paymentEvent = new NotificationPaymentEvent(1L, 1L, "123", "FAILED", LocalDateTime.now());

        notificationConsumer.consumePaymentEvent(paymentEvent);

        verify(notificationService).sendNotification(
                eq("1"),
                eq("Payment Issue"),
                eq("There was an issue with your payment for order #1. Please try again.")
        );
    }

    @Test
    public void testConsumePaymentEvent_IncompleteEvent() {
        NotificationPaymentEvent paymentEvent = new NotificationPaymentEvent(null, 1L, "123", "SUCCESS", LocalDateTime.now());

        notificationConsumer.consumePaymentEvent(paymentEvent);

        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString());
    }
}