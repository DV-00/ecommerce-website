package com.ecommerce.cartservice.dtos;

import lombok.Data;


@Data
public class CartResponseDTO {

    private Long id;

    private Long productId;

    private String title;

    private int quantity;

    private double price;


    // All-args constructor
    public CartResponseDTO(Long id, Long productId, String title, int quantity, double price) {
        this.id = id;
        this.productId = productId;
        this.title = title;
        this.quantity = quantity;
        this.price = price;
    }
}
