package com.ecommerce.cartservice.controllers;

import com.ecommerce.cartservice.dtos.CartRequestDTO;
import com.ecommerce.cartservice.dtos.CartResponseDTO;
import com.ecommerce.cartservice.dtos.UpdateCartItemDTO;
import com.ecommerce.cartservice.services.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@Valid @RequestBody CartRequestDTO requestDTO) {
        return ResponseEntity.ok(cartService.addToCart(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<CartResponseDTO>> getUserCart(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sessionId) {

        if (userId == null && (sessionId == null || sessionId.isEmpty())) {
            return ResponseEntity.badRequest().build(); // Ensures at least one identifier is provided
        }

        return ResponseEntity.ok(cartService.getUserCart(userId, sessionId));
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeCart(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull String sessionId) {

        if (sessionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        cartService.mergeCart(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemDTO updateDTO) {

        return ResponseEntity.ok(cartService.updateCartItem(cartItemId, updateDTO));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemCount(@RequestParam Long userId) {
        return cartService.getCartItemCount(userId);
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getCartTotal(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getCartTotal(userId));
    }
}

