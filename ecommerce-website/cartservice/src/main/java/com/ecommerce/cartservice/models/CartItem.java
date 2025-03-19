package com.ecommerce.cartservice.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cart_items")
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    private Long userId;
    private String sessionId;

    public CartItem() {}

    public CartItem(Long id, Long productId, int quantity, Long userId, String sessionId) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.userId = userId;
        this.sessionId = sessionId;
    }
}
