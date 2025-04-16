package com.ecommerce.productservice.services;

import com.ecommerce.productservice.dtos.CreateProductRequestDto;
import com.ecommerce.productservice.dtos.UpdateProductPriceDto;
import com.ecommerce.productservice.exceptions.UnauthorizedException;
import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.repositories.CategoryRepository;
import com.ecommerce.productservice.repositories.ProductRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    private static MockWebServer mockWebServer;
    private WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private ProductServiceImpl productService;


    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private Product sampleProduct;
    private Category sampleCategory;
    private final String adminToken = "valid_admin_token";

    @BeforeAll
    static void setupAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void setUp() {
        // Prepare sample data
        sampleCategory = new Category();
        sampleCategory.setName("Electronics");

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setTitle("Smartphone");
        sampleProduct.setPrice(500.0);
        sampleProduct.setCategory(sampleCategory);
        sampleProduct.setQuantity(10);

        String baseUrl = mockWebServer.url("/").toString();
        webClientBuilder = WebClient.builder().baseUrl(baseUrl);
        webClient = webClientBuilder.build();

        productService = new ProductServiceImpl(productRepository, categoryRepository, webClientBuilder);

        ReflectionTestUtils.setField(productService, "webClient", webClient);
    }

    @Test
    public void testValidateAdminRole_Success() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"role\": \"ADMIN\"}")
                .addHeader("Content-Type", "application/json"));

        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(sampleCategory));
        when(productRepository.findByTitleAndCategory(anyString(), any(Category.class))).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        assertDoesNotThrow(() -> productService.createProduct(adminToken,
                new CreateProductRequestDto("img.png", "Smartphone", "Latest model", "Electronics", 500.0, 10)));
    }

    @Test
    public void testValidateAdminRole_Unauthorized() throws Exception {
        // Enqueue a 401 from MockWebServer
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"message\":\"Unauthorized\"}")
                .addHeader("Content-Type", "application/json"));

        // invoke validateAdminRole via reflection and expect the exception
        assertThrows(UnauthorizedException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        productService,
                        "validateAdminRole",
                        adminToken
                )
        );
    }

    @Test
    public void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product product = productService.getProductById(1L);

        assertNotNull(product);
        assertEquals("Smartphone", product.getTitle());
    }

    @Test
    public void testGetAllProducts_Success() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Product> result = productService.getAllProducts(Pageable.ofSize(10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Smartphone", result.getContent().get(0).getTitle());
    }

    @Test
    public void testCreateProduct_Success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"role\": \"ADMIN\"}")
                .addHeader("Content-Type", "application/json"));

        when(categoryRepository.findByName(any())).thenReturn(Optional.of(sampleCategory));
        when(productRepository.findByTitleAndCategory(anyString(), any(Category.class))).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product createdProduct = productService.createProduct(adminToken,
                new CreateProductRequestDto("img", "Latest model", "desc", "c name", 50.50, 10));

        assertNotNull(createdProduct);
        assertEquals("Smartphone", createdProduct.getTitle());
    }

    @Test
    public void testUpdateProductPrice_Success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"role\": \"ADMIN\"}")
                .addHeader("Content-Type", "application/json"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        UpdateProductPriceDto updateDto = new UpdateProductPriceDto();
        updateDto.setUpdatedPrice(600.0);

        Product updatedProduct = productService.updateProductPrice(adminToken, 1L, updateDto);

        assertNotNull(updatedProduct);
        assertEquals(600.0, updatedProduct.getPrice());
    }

    @Test
    public void testDeleteProduct_Success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"role\": \"ADMIN\"}")
                .addHeader("Content-Type", "application/json"));

        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        boolean result = productService.deleteProduct(adminToken, 1L);
        assertTrue(result);
    }
}