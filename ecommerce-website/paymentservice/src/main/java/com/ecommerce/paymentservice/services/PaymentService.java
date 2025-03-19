package com.ecommerce.paymentservice.services;

import com.ecommerce.paymentservice.dtos.OrderEvent;
import com.ecommerce.paymentservice.dtos.PaymentEvent;

public interface PaymentService {

    public String createPaymentLink(Long orderId, Long userId, Double amount);

    PaymentEvent processPayment(OrderEvent orderEvent);

    void processPaymentCallback(String paymentId, String status);

    boolean processPayment(String paymentId, String status);
}
