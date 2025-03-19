package com.ecommerce.paymentservice.controllers;

import com.ecommerce.paymentservice.dtos.CreatePaymentLinkRequestDto;
import com.ecommerce.paymentservice.dtos.PaymentLinkResponseDto;
import com.ecommerce.paymentservice.dtos.PaymentResponseDto;
import com.ecommerce.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Generate Payment Link
    @PostMapping("/create-payment-link")
    public ResponseEntity<PaymentLinkResponseDto> createPayment(@RequestBody CreatePaymentLinkRequestDto requestDto) {
        String paymentLink = paymentService.createPaymentLink(requestDto.getOrderId(), requestDto.getUserId(), requestDto.getAmount());
        return ResponseEntity.ok(new PaymentLinkResponseDto(paymentLink));
    }


    // Process Payment based on dynamic link
    @PostMapping("/process/{paymentId}")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @PathVariable String paymentId,
            @RequestParam String status) {
        boolean success = paymentService.processPayment(paymentId, status);
        return ResponseEntity.ok(new PaymentResponseDto(paymentId, success ? "SUCCESS" : "FAILED"));
    }
}
