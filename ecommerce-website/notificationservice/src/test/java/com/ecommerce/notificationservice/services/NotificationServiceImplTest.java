package com.ecommerce.notificationservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationServiceImplTest {

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl();
    }

    @Test
    void testSendNotificationRunsWithoutError() {
        String userId = "123";
        String subject = "Order Placed";
        String message = "Your order has been placed successfully.";

        notificationService.sendNotification(userId, subject, message);
    }
}
