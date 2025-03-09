package com.ecommerce.cartservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartItemDTO {
    @NotNull
    private Long productId;

    @Min(1)
    private int quantity;
}
