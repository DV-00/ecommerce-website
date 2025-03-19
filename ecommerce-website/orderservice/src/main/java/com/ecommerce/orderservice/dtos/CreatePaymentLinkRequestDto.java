package com.ecommerce.orderservice.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePaymentLinkRequestDto {
    private Long orderId;
    private Double amount;
    private Long userId;
}
