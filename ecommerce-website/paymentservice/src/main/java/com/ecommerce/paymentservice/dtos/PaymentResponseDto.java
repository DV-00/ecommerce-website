package com.ecommerce.paymentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponseDto {
    private String paymentId;
    private String status;
}
