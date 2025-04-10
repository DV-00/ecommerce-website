package com.ecommerce.orderservice.dtos;

import com.ecommerce.orderservice.models.PaymentStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("status")
    private PaymentStatusEnum status;

    @JsonProperty("event_time")
    private LocalDateTime eventTime;
}
