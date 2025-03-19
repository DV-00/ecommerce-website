package com.ecommerce.cartservice.services;

import com.ecommerce.cartservice.dtos.CartRequestDTO;
import com.ecommerce.cartservice.dtos.CartResponseDTO;
import com.ecommerce.cartservice.dtos.UpdateCartItemDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface CartService {

    CartResponseDTO addToCart(CartRequestDTO requestDTO);

    List<CartResponseDTO> getUserCart(Long userId, String sessionId);

    void mergeCart(Long userId, String sessionId);

    void removeCartItem(Long cartItemId);

    CartResponseDTO updateCartItem(Long cartItemId, UpdateCartItemDTO updateDTO);

    void clearCart(Long userId);

    ResponseEntity<Map<String, Integer>> getCartItemCount(Long userId);

    Double getCartTotal(Long userId);

}
