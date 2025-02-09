package com.ecommerce.productservice.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

@Data
public class Category {
    private String name;

    @JsonCreator
    public Category(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
