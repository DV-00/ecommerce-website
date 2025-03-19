package com.ecommerce.notificationservice.kafka;

import com.ecommerce.notificationservice.dtos.NotificationOrderEvent;
import com.ecommerce.notificationservice.dtos.NotificationPaymentEvent;
import com.ecommerce.notificationservice.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;

    // Constructor
    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ---------- Consume Order Event ----------
    @KafkaListener(topics = "order-events", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderEvent(NotificationOrderEvent orderEvent) {

        if (orderEvent == null) {
            logger.warn("Received null OrderEvent");
            return;
        }
        logger.info("Received OrderEvent: {}", orderEvent);

        if (orderEvent.getOrderId() == null || orderEvent.getUserId() == null || orderEvent.getStatus() == null) {
            logger.warn("Incomplete OrderEvent received: {}", orderEvent);
            return;
        }

        if ("CANCELED".equalsIgnoreCase(orderEvent.getStatus())) {
            notificationService.sendNotification(
                    String.valueOf(orderEvent.getUserId()),
                    "Order Cancellation",
                    "Your order #" + orderEvent.getOrderId() + " has been canceled due to non-payment."
            );
        }
    }

    // ---------- Consume Payment Event ----------
    @KafkaListener(topics = "payment-events", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvent(NotificationPaymentEvent paymentEvent) {

        if (paymentEvent == null) {
            logger.warn("Received null PaymentEvent");
            return;
        }
        logger.info("Received PaymentEvent: {}", paymentEvent);

        if (paymentEvent.getOrderId() == null || paymentEvent.getUserId() == null || paymentEvent.getStatus() == null) {
            logger.warn("Incomplete PaymentEvent received: {}", paymentEvent);
            return;
        }

        switch (paymentEvent.getStatus().toUpperCase()) {
            case "SUCCESS":
                notificationService.sendNotification(
                        String.valueOf(paymentEvent.getUserId()),
                        "Payment Successful",
                        "Your payment for order #" + paymentEvent.getOrderId() + " was successful."
                );
                break;

            case "FAILED":
            case "CANCELED":
                notificationService.sendNotification(
                        String.valueOf(paymentEvent.getUserId()),
                        "Payment Issue",
                        "There was an issue with your payment for order #" + paymentEvent.getOrderId() + ". Please try again."
                );
                break;

            default:
                logger.warn("Unrecognized payment status: {}", paymentEvent.getStatus());
        }
    }

}
