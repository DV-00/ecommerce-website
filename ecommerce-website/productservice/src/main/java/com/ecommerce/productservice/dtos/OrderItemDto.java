package com.ecommerce.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Integer quantity;
}
