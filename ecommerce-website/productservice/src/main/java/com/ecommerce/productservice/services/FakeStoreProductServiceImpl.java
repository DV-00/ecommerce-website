package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.dtos.FakeProductProductResponseDto;
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

    private Product convertToProduct(FakeProductProductResponseDto dto) {
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
                .bodyToMono(FakeProductProductResponseDto.class)
                .map(this::convertToProduct)  // Convert DTO to Product
                .block();
    }

    @Override
    public List<Product> getAllProducts() {
        return webClient.get()
                .uri("/products")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new RuntimeException("Failed to fetch products from FakeStore API"))
                )
                .bodyToFlux(FakeProductProductResponseDto.class)
                .map(this::convertToProduct)  // Convert DTO to Product
                .collectList()
                .block();
    }

    @Override
    public Product createProduct(String title, String descrption, String image, double price, String category) {
        return null;
    }

    @Override
    public Product updateProductPrice(long productId, double updatedPrice) {
        return null;
    }

    @Override
    public Product updateProductImage(long productId, String updatedImage) {
        return null;
    }

    @Override
    public boolean deleteProduct(long ProductId) {
        return false;
    }

}
