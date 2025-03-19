package com.ecommerce.paymentservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("orderId")
    private Long orderId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("totalAmount")
    private Double totalAmount;

    @JsonProperty("items")
    private List<OrderItem> items;

    @JsonProperty("status")
    private String status;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
}
