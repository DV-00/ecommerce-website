package com.ecommerce.cartservice.dtos;

import lombok.Data;

@Data
public class ProductDetailsDTO {
    private Long id;
    private String title;
    private double price;
    private int stock;
}
