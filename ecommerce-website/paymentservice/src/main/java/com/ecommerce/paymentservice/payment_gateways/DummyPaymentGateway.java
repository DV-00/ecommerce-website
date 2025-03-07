package com.ecommerce.paymentservice.payment_gateways;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Primary
public class DummyPaymentGateway implements PaymentGatwayAdapter {

    @Override
    public String createPaymentLink(long orderId, long amount) {

        String transactionId = UUID.randomUUID().toString();
        return "https://dummy-payment.com/pay?orderId=" + orderId + "&amount=" + amount + "&txn=" + transactionId;
    }
}
