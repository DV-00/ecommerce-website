package com.ecommerce.productservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProductImageDto {

    @NotBlank(message = "Updated image URL is required")
    private String updatedImage;
}
