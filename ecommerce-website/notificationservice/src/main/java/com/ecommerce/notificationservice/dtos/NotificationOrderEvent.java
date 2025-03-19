package com.ecommerce.notificationservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationOrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("orderId")
    private Long orderId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("totalAmount")
    private Double totalAmount;

    @JsonProperty("items")
    private List<NotificationOrderItem> items;

    @JsonProperty("status")
    private String status;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    // Constructor
    public NotificationOrderEvent() {}

    public NotificationOrderEvent(Long orderId, Long userId, Double totalAmount, List<NotificationOrderItem> items, String status, LocalDateTime expiresAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.items = items;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public List<NotificationOrderItem> getItems() { return items; }
    public void setItems(List<NotificationOrderItem> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

}
