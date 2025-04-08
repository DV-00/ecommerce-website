package com.ecommerce.cartservice.services;

import com.ecommerce.cartservice.dtos.CartRequestDTO;
import com.ecommerce.cartservice.dtos.CartResponseDTO;
import com.ecommerce.cartservice.dtos.ProductDetailsDTO;
import com.ecommerce.cartservice.dtos.UpdateCartItemDTO;
import com.ecommerce.cartservice.exceptions.ResourceNotFoundException;
import com.ecommerce.cartservice.models.CartItem;
import com.ecommerce.cartservice.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final WebClient.Builder webClientBuilder;
    //private static final String PRODUCT_SERVICE_URL = "http://localhost:8081/products";

    @Value("${productservice.base-url}/products")
    private String productServiceUrl;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, WebClient.Builder webClientBuilder) {
        this.cartRepository = cartRepository;
        this.webClientBuilder = webClientBuilder;
    }

    // -------------------------------- Create --------------------------------

    @Override
    @Transactional
    public CartResponseDTO addToCart(CartRequestDTO requestDTO) {
        Long productId = requestDTO.getProductId();
        int quantity = requestDTO.getQuantity();

        boolean isStockAvailable = checkProductStock(productId, quantity);
        if (!isStockAvailable) throw new ResourceNotFoundException("Product is out of stock");

        CartItem cartItem = new CartItem();
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        if (requestDTO.getUserId() != null) cartItem.setUserId(requestDTO.getUserId());
        else cartItem.setSessionId(requestDTO.getSessionId());

        cartItem = cartRepository.save(cartItem);
        ProductDetailsDTO productDetails = fetchProductDetails(productId);

        return new CartResponseDTO(cartItem.getId(), cartItem.getProductId(), productDetails.getTitle(),
                cartItem.getQuantity(), productDetails.getPrice(), cartItem.getUserId());
    }

    // -------------------------------- Read --------------------------------

    @Override
    public List<CartResponseDTO> getUserCart(Long userId, String sessionId) {
        List<CartItem> cartItems = (userId != null) ? cartRepository.findByUserId(userId)
                : cartRepository.findBySessionId(sessionId);

        return cartItems.stream().map(item -> {
            ProductDetailsDTO productDetails = fetchProductDetails(item.getProductId());
            return new CartResponseDTO(item.getId(), item.getProductId(), productDetails.getTitle(),
                    item.getQuantity(), productDetails.getPrice(), item.getUserId());
        }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<Map<String, Integer>> getCartItemCount(Long userId) {
        Integer count = cartRepository.countByUserId(userId);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @Override
    public Double getCartTotal(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        return cartItems.stream().mapToDouble(item -> {
            ProductDetailsDTO productDetails = fetchProductDetails(item.getProductId());
            return productDetails.getPrice() * item.getQuantity();
        }).sum();
    }

    // -------------------------------- Update --------------------------------

    @Override
    public CartResponseDTO updateCartItem(Long cartItemId, UpdateCartItemDTO updateDTO) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItem.setQuantity(updateDTO.getQuantity());
        cartRepository.save(cartItem);
        ProductDetailsDTO productDetails = fetchProductDetails(cartItem.getProductId());

        return new CartResponseDTO(cartItem.getId(), cartItem.getProductId(), productDetails.getTitle(),
                cartItem.getQuantity(), productDetails.getPrice(), cartItem.getUserId());
    }

    @Override
    @Transactional
    public void mergeCart(Long userId, String sessionId) {
        if (userId == null || sessionId == null)
            throw new IllegalArgumentException("Both userId and sessionId are required for merging carts");

        List<CartItem> guestCartItems = cartRepository.findBySessionId(sessionId);
        guestCartItems.forEach(item -> {
            item.setUserId(userId);
            item.setSessionId(null);
        });

        cartRepository.saveAll(guestCartItems);
    }

    // -------------------------------- Delete --------------------------------

    public void removeCartItem(Long cartItemId) {
        if (!cartRepository.existsById(cartItemId))
            throw new RuntimeException("Cart item not found with ID: " + cartItemId);
        cartRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        int deletedCount = cartRepository.countByUserId(userId);
        if (deletedCount == 0) throw new ResourceNotFoundException("No cart items found for user ID: " + userId);
        cartRepository.deleteByUserId(userId);
    }

    // -------------------------------- Helpers --------------------------------

    private boolean checkProductStock(Long productId, int quantity) {
        try {
            Integer stock = webClientBuilder.build().get()
                    .uri(productServiceUrl + "/{id}/stock", productId)
                    .retrieve().bodyToMono(Integer.class)
                    .defaultIfEmpty(0).block();
            return stock >= quantity;
        } catch (Exception e) {
            return false;
        }
    }

    private ProductDetailsDTO fetchProductDetails(Long productId) {
        return webClientBuilder.build().get()
                .uri(productServiceUrl + "/{id}", productId)
                .retrieve().bodyToMono(ProductDetailsDTO.class)
                .block();
    }
}
