package com.ecommerce.notificationservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;

public class NotificationPaymentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("event_time")
    private LocalDateTime eventTime;

    // Constructor
    public NotificationPaymentEvent() {}

    public NotificationPaymentEvent(Long orderId, Long userId, String paymentId, String status, LocalDateTime eventTime) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentId = paymentId;
        this.status = status;
        this.eventTime = eventTime;
    }

    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

}
