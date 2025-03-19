package com.ecommerce.paymentservice.dtos;

import com.ecommerce.paymentservice.models.PaymentStatus;
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
    private PaymentStatus status;

    @JsonProperty("event_time")
    private LocalDateTime eventTime;
}
