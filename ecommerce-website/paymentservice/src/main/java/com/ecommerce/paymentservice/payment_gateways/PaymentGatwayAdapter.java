package com.ecommerce.paymentservice.payment_gateways;

public interface PaymentGatwayAdapter {

    public String createPaymentLink(long orderId, long amount);

}
