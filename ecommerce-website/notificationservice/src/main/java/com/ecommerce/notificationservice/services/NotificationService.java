package com.ecommerce.notificationservice.services;

public interface NotificationService {

    void sendNotification(String userId, String subject, String message);

}
