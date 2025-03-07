package com.ecommerce.paymentservice.controllers;

import com.ecommerce.paymentservice.dtos.CreatePaymentLinkRequestDto;
import com.ecommerce.paymentservice.payment_gateways.PaymentGatwayAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentGatwayAdapter paymentGatwayAdapter;

    public PaymentController(PaymentGatwayAdapter paymentGatwayAdapter) {
        this.paymentGatwayAdapter = paymentGatwayAdapter;
    }

    @PostMapping("/create-payment-link")
    public ResponseEntity<String> createPayment(@RequestBody CreatePaymentLinkRequestDto paymentRequest) {
        try {
            String paymentLink = paymentGatwayAdapter.createPaymentLink(paymentRequest.getOrderId(), paymentRequest.getAmount());
            return ResponseEntity.ok(paymentLink);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating payment link: " + e.getMessage());
        }
    }

}
