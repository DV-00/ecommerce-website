package com.ecommerce.cartservice.dtos;

import lombok.Data;

@Data
public class CartResponseDTO {
    private Long id;
    private Long productId;
    private String title;
    private int quantity;
    private double price;
    private Long userId;

    public CartResponseDTO(Long id, Long productId, String title, int quantity, double price, Long userId) {
        this.id = id;
        this.productId = productId;
        this.title = title;
        this.quantity = quantity;
        this.price = price;
        this.userId = userId;
    }
}
