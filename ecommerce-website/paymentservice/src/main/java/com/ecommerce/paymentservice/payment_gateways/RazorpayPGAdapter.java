package com.ecommerce.paymentservice.payment_gateways;

import org.springframework.stereotype.Component;

@Component
public class RazorpayPGAdapter implements PaymentGatwayAdapter{

    @Override
    public String createPaymentLink(long orderId, long amount) {

        return "payment_link";
    }
}
