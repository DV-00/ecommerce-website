package com.ecommerce.cartservice.controllers;

import com.ecommerce.cartservice.dtos.CartRequestDTO;
import com.ecommerce.cartservice.dtos.CartResponseDTO;
import com.ecommerce.cartservice.services.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Map;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void addToCart() throws Exception {
        CartResponseDTO responseDTO = new CartResponseDTO(1L, 1L, "Product Title", 2, 50.0, 1L);
        when(cartService.addToCart(any(CartRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\": 1, \"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.quantity", is(2)));
    }

    @Test
    void mergeCart() throws Exception {
        mockMvc.perform(post("/api/cart/merge")
                        .param("userId", "1")
                        .param("sessionId", "session123"))
                .andExpect(status().isOk());
    }

    @Test
    void removeCartItem() throws Exception {
        mockMvc.perform(delete("/api/cart/remove/1"))
                .andExpect(status().isOk());
    }

    @Test
    void clearCart() throws Exception {
        mockMvc.perform(delete("/api/cart/clear")
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCartItemCount() throws Exception {
        Map<String, Integer> itemCount = Map.of("count", 5);
        when(cartService.getCartItemCount(anyLong())).thenReturn(ResponseEntity.ok(itemCount));

        mockMvc.perform(get("/api/cart/count")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));
    }

    @Test
    void getCartTotal() throws Exception {
        when(cartService.getCartTotal(anyLong())).thenReturn(100.0);

        mockMvc.perform(get("/api/cart/total")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(100.0)));
    }
}