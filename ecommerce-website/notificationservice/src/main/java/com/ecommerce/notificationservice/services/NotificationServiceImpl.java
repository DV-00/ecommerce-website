package com.ecommerce.notificationservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void sendNotification(String userId, String subject, String message) {
        // Log the simulated email sending
        logger.info("Simulating sending email to user {}: Subject: '{}', Message: '{}'", userId, subject, message);
    }
}
