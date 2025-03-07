package com.ecommerce.productservice.dtos;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateProductPriceDto {

    @Min(value = 1, message = "Updated price must be greater than 0")
    private double updatedPrice;
}
