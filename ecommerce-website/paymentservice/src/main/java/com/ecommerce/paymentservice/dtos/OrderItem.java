package com.ecommerce.paymentservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("quantity")
    private int quantity;
}
