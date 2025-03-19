package com.ecommerce.productservice.services;

import com.ecommerce.productservice.dtos.*;
import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Service("fakestore")
public class FakeStoreProductServiceImpl implements ProductService {

    private final WebClient webClient;

    public FakeStoreProductServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://fakestoreapi.com").build();
    }

    private Product convertToProduct(FakeProductResponseDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setTitle(dto.getTitle());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());

        Category category = new Category();
        category.setName(dto.getCategory());
        product.setCategory(category);

        return product;
    }

    @Override
    public Product getProductById(long id) {
        return webClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Failed to fetch product with ID: " + id))
                )
                .bodyToMono(FakeProductResponseDto.class)
                .map(this::convertToProduct)
                .block();
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        List<Product> productList = webClient.get()
                .uri("/products")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Failed to fetch products from FakeStore API"))
                )
                .bodyToFlux(FakeProductResponseDto.class)
                .map(this::convertToProduct)  // Convert DTO to Product
                .collectList()
                .block();

        // implement pagination using Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productList.size());

        List<Product> paginatedProducts = productList.subList(start, end);
        return new PageImpl<>(paginatedProducts, pageable, productList.size());
    }

    @Override
    public Product createProduct(String token, CreateProductRequestDto createProductRequestDto) {
        return null;
    }

    @Override
    public Product updateProductPrice(String token, long productId, UpdateProductPriceDto updateProductPriceDto) {
        return null;
    }

    @Override
    public Product updateProductImage(String token, long productId, UpdateProductImageDto updateProductImageDto) {
        return null;
    }

    @Override
    public Product updateProductQuantity(String token, long productId, UpdateProductQuantityDto updateProductQuantityDto) {
        return null;
    }

    @Override
    public boolean deleteProduct(String token, long productId) {
        return false;
    }

    @Override
    public int getProductStock(long productId) {
        return 0;
    }

    @Override
    public boolean reduceStock(long productId, int quantity) {
        return false;
    }

    @Override
    public void increaseStock(long productId, int quantity) {

    }

    @Override
    public void restoreStock(Long productId, Integer quantity) {

    }
}
