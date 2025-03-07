package com.ecommerce.paymentservice.dtos;

import lombok.Data;


@Data
public class CreatePaymentLinkRequestDto {

    long orderId;
    long amount;

}
