package com.ecommerce.cartservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CartRequestDTO {

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String sessionId; // For guest users

    private Long userId; // For logged-in users


    // Ensure at least one identifier (userId or sessionId) is present
    public boolean isValid() {
        return (userId != null) || (sessionId != null && !sessionId.trim().isEmpty());
    }
}
