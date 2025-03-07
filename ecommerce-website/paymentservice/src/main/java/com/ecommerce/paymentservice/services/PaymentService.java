package com.ecommerce.paymentservice.services;

public interface PaymentService {

    public String createPaymentLink(long orderId, long amount) throws Exception;


}