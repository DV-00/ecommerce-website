package com.ecommerce.cartservice.services;

import com.ecommerce.cartservice.dtos.CartRequestDTO;
import com.ecommerce.cartservice.dtos.CartResponseDTO;
import com.ecommerce.cartservice.dtos.ProductDetailsDTO;
import com.ecommerce.cartservice.dtos.UpdateCartItemDTO;
import com.ecommerce.cartservice.exceptions.ResourceNotFoundException;
import com.ecommerce.cartservice.models.CartItem;
import com.ecommerce.cartservice.repositories.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private CartServiceImpl cartService;

    private WebClient webClient;

    @BeforeEach
    public void setUp() {
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    public void testAddToCart() {
        CartRequestDTO requestDTO = new CartRequestDTO();
        requestDTO.setProductId(101L);
        requestDTO.setQuantity(2);
        requestDTO.setSessionId("session123");

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProductId(101L);
        cartItem.setQuantity(2);
        when(cartRepository.save(any(CartItem.class))).thenReturn(cartItem);

        ProductDetailsDTO productDetails = new ProductDetailsDTO();
        productDetails.setId(101L);
        productDetails.setTitle("Product");
        productDetails.setPrice(100.0);
        when(webClient.get().uri(anyString(), any(Long.class)).retrieve().bodyToMono(ProductDetailsDTO.class))
                .thenReturn(Mono.just(productDetails));
        when(webClient.get().uri(anyString(), any(Long.class)).retrieve().bodyToMono(Integer.class))
                .thenReturn(Mono.just(10));

        CartResponseDTO responseDTO = cartService.addToCart(requestDTO);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getProductId()).isEqualTo(101L);
        assertThat(responseDTO.getQuantity()).isEqualTo(2);
        assertThat(responseDTO.getTitle()).isEqualTo("Product");
        assertThat(responseDTO.getPrice()).isEqualTo(100.0);
    }

    @Test
    public void testGetUserCart() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProductId(101L);
        cartItem.setQuantity(2);
        cartItem.setUserId(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem));

        ProductDetailsDTO productDetails = new ProductDetailsDTO();
        productDetails.setId(101L);
        productDetails.setTitle("Product");
        productDetails.setPrice(100.0);
        when(webClient.get().uri(anyString(), any(Long.class)).retrieve().bodyToMono(ProductDetailsDTO.class))
                .thenReturn(Mono.just(productDetails));

        List<CartResponseDTO> cartItems = cartService.getUserCart(1L, null);

        assertThat(cartItems).isNotEmpty();
        assertThat(cartItems.get(0).getProductId()).isEqualTo(101L);
        assertThat(cartItems.get(0).getTitle()).isEqualTo("Product");
    }

    @Test
    public void testGetCartItemCount() {
        when(cartRepository.countByUserId(1L)).thenReturn(2);

        ResponseEntity<Map<String, Integer>> response = cartService.getCartItemCount(1L);

        assertThat(response.getBody()).containsEntry("count", 2);
    }

    @Test
    public void testGetCartTotal() {
        CartItem cartItem = new CartItem();
        cartItem.setProductId(101L);
        cartItem.setQuantity(2);
        when(cartRepository.findByUserId(1L)).thenReturn(Arrays.asList(cartItem));

        ProductDetailsDTO productDetails = new ProductDetailsDTO();
        productDetails.setId(101L);
        productDetails.setTitle("Product");
        productDetails.setPrice(100.0);
        when(webClient.get().uri(anyString(), any(Long.class)).retrieve().bodyToMono(ProductDetailsDTO.class))
                .thenReturn(Mono.just(productDetails));

        Double total = cartService.getCartTotal(1L);

        assertThat(total).isEqualTo(200.0);
    }

    @Test
    public void testUpdateCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProductId(101L);
        cartItem.setQuantity(2);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(cartItem)).thenReturn(cartItem);

        ProductDetailsDTO productDetails = new ProductDetailsDTO();
        productDetails.setId(101L);
        productDetails.setTitle("Product");
        productDetails.setPrice(100.0);
        when(webClient.get().uri(anyString(), any(Long.class)).retrieve().bodyToMono(ProductDetailsDTO.class))
                .thenReturn(Mono.just(productDetails));

        UpdateCartItemDTO updateDTO = new UpdateCartItemDTO();
        updateDTO.setProductId(101L);
        updateDTO.setQuantity(3);

        CartResponseDTO responseDTO = cartService.updateCartItem(1L, updateDTO);

        assertThat(responseDTO.getQuantity()).isEqualTo(3);
    }

    @Test
    public void testMergeCart() {
        CartItem cartItem = new CartItem();
        cartItem.setProductId(101L);
        cartItem.setQuantity(2);
        cartItem.setSessionId("session123");
        when(cartRepository.findBySessionId("session123")).thenReturn(Arrays.asList(cartItem));
        when(cartRepository.saveAll(any())).thenReturn(Arrays.asList(cartItem));

        cartService.mergeCart(1L, "session123");

        verify(cartRepository, times(1)).saveAll(any());
    }

    @Test
    public void testRemoveCartItem() {
        when(cartRepository.existsById(1L)).thenReturn(true);

        cartService.removeCartItem(1L);

        verify(cartRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testClearCart() {
        when(cartRepository.countByUserId(1L)).thenReturn(1);

        cartService.clearCart(1L);

        verify(cartRepository, times(1)).deleteByUserId(1L);
    }

    @Test
    public void testAddToCart_ProductOutOfStock() {
        CartRequestDTO requestDTO = new CartRequestDTO();
        requestDTO.setProductId(101L);
        requestDTO.setQuantity(2);
        requestDTO.setSessionId("session123");

        when(webClient.get().uri(anyString(), any(Long.class)).retrieve().bodyToMono(Integer.class))
                .thenReturn(Mono.just(0));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(requestDTO));
    }

    @Test
    public void testAddToCart_InvalidRequest() {
        CartRequestDTO requestDTO = new CartRequestDTO();
        requestDTO.setProductId(101L);
        requestDTO.setQuantity(2);

        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(requestDTO));
    }
}