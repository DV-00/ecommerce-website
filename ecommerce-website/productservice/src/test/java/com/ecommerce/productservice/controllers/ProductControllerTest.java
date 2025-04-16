package com.ecommerce.productservice.controllers;

import com.ecommerce.productservice.dtos.CreateProductRequestDto;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private RedisTemplate redisTemplate;

    @Test
    void testGetProductById() throws Exception {
        Product dummyProduct = new Product();
        dummyProduct.setId(1L);
        when(productService.getProductById(1L)).thenReturn(dummyProduct);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetProducts() throws Exception {
        Page<Product> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(productService.getAllProducts(any())).thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateProduct() throws Exception {
        Product dummyProduct = new Product();
        dummyProduct.setId(100L);

        CreateProductRequestDto createDto = new CreateProductRequestDto("image.jpg", "Product1", "Description", "Electronics", 100.0, 10);
        createDto.setTitle("Test Product");
        createDto.setDescription("Test Description");
        createDto.setCategoryName("Test Category");
        createDto.setPrice(10.0);
        createDto.setQuantity(5);

        when(productService.createProduct(eq("Bearer dummyToken"), any(CreateProductRequestDto.class)))
                .thenReturn(dummyProduct);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer dummyToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void testDeleteProduct() throws Exception {
        when(productService.deleteProduct(eq("Bearer dummyToken"), eq(50L)))
                .thenReturn(true);

        mockMvc.perform(delete("/products/50")
                        .header("Authorization", "Bearer dummyToken"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetStockByProductId() throws Exception {
        when(productService.getProductStock(1L)).thenReturn(42);

        mockMvc.perform(get("/products/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(42));
    }
}
