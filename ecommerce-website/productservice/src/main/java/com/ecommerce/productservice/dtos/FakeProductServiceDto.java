package com.ecommerce.productservice.dtos;

import com.ecommerce.productservice.models.Category;
import lombok.Data;

@Data
public class FakeProductServiceDto {

    private long id;

    private String title;

    private double price;

    private String description;

    private String image;

    private String category;

}
