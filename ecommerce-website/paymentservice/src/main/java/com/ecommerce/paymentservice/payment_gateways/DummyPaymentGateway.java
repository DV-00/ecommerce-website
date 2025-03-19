package com.ecommerce.paymentservice.payment_gateways;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Primary
public class DummyPaymentGateway implements PaymentGatewayAdapter {

    @Value("${paymentservice.gateway.base-url}")
    private String paymentServiceBaseUrl;

    @Override
    public String createPaymentLink(long orderId, BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();
        return paymentServiceBaseUrl + "/payments/process/" + transactionId;
    }
}
