package com.ecommerce.productservice.dtos;

import lombok.Data;

@Data
public class CreateProductRequestDto {

    private String image;

    private String title;

    private String description;

    private String categoryName;

    private double price;

}
