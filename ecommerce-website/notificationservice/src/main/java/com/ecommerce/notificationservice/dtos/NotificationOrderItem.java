package com.ecommerce.notificationservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class NotificationOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("quantity")
    private Integer quantity;

    // Constructor
    public NotificationOrderItem() {}

    public NotificationOrderItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters & Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

}
