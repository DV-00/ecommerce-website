package com.ecommerce.paymentservice.services;

import com.ecommerce.paymentservice.payment_gateways.PaymentGatwayAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService{

    PaymentGatwayAdapter paymentGatwayAdapter;

    @Autowired
    public PaymentServiceImpl(PaymentGatwayAdapter paymentGatwayAdapter) {
        this.paymentGatwayAdapter = paymentGatwayAdapter;
    }

    @Override
    public String createPaymentLink(long orderId, long amount) throws Exception {
        //to do call the user service and order service
        return paymentGatwayAdapter.createPaymentLink(orderId, amount);
    }
}
