package com.ecommerce.productservice.models;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class Product {

    private long id;

    private String title;

    private String description;

    private String image;

    private Category category;

}
