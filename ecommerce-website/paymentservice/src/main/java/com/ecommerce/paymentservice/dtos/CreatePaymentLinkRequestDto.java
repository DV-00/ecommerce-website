package com.ecommerce.paymentservice.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePaymentLinkRequestDto {

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be greater than 0")
    private Long orderId;

    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be greater than 0")
    private Long userId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}
