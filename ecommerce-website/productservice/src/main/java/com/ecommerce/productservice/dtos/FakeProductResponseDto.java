package com.ecommerce.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FakeProductResponseDto {

        private long id;

        private String title;

        private double price;

        private String description;

        private String image;

        private String category;
}
