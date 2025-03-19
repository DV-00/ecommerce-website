package com.ecommerce.paymentservice.payment_gateways;

import java.math.BigDecimal;

public interface PaymentGatewayAdapter {

    String createPaymentLink(long orderId, BigDecimal amount);

}
