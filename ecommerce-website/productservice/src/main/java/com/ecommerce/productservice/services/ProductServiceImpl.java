package com.ecommerce.productservice.services;

import com.ecommerce.productservice.dtos.CreateProductRequestDto;
import com.ecommerce.productservice.dtos.UpdateProductImageDto;
import com.ecommerce.productservice.dtos.UpdateProductPriceDto;
import com.ecommerce.productservice.dtos.UpdateProductQuantityDto;
import com.ecommerce.productservice.exceptions.UnauthorizedException;
import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.repositories.CategoryRepository;
import com.ecommerce.productservice.repositories.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;


@Primary
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final WebClient webClient;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, WebClient.Builder webClientBuilder) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/").build();
    }


    private void validateAdminRole(String token) {
        try {
            String response = webClient.get()
                    .uri("/users/validate?token=" + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        throw new UnauthorizedException("Invalid token or access denied.");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        throw new RuntimeException("User Service is unavailable. Please try again later.");
                    })
                    .bodyToMono(String.class)
                    .block();


            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            String role = jsonNode.get("role").asText();

            if (!"ADMIN".equals(role)) {
                throw new UnauthorizedException("Access denied! Only admins can perform this action.");
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token or access denied.");
        }
    }


    // GET ALL PRODUCTS WITH PAGINATION + CACHING
    @Cacheable(value = "products", key = "#pageable.pageNumber")
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // GET SINGLE PRODUCT WITH CACHING
    @Cacheable(value = "product", key = "#id")
    @Override
    public Product getProductById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }


    @CacheEvict(value = {"product", "products"}, allEntries = true)
    @Override
    public Product createProduct(String token, CreateProductRequestDto dto) {
        validateAdminRole(token);
        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(dto.getCategoryName());
                    return categoryRepository.save(newCategory);
                });

        Optional<Product> existingProduct = productRepository.findByTitleAndCategory(dto.getTitle(), category);
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with this title already exists in the category.");
        }

        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());
        product.setPrice(dto.getPrice());
        product.setCategory(category);
        product.setQuantity(dto.getQuantity());

        return productRepository.save(product);
    }


    @CacheEvict(value = {"product", "products"}, allEntries = true)
    @Override
    public Product updateProductPrice(String token, long productId, UpdateProductPriceDto updateProductPriceDto) {
        validateAdminRole(token);
        Product product = getProductById(productId);
        product.setPrice(updateProductPriceDto.getUpdatedPrice());
        return productRepository.save(product);
    }

    @CacheEvict(value = {"product", "products"}, allEntries = true)
    @Override
    public Product updateProductImage(String token, long productId, UpdateProductImageDto updateProductImageDto) {
        validateAdminRole(token);
        Product product = getProductById(productId);
        product.setImage(updateProductImageDto.getUpdatedImage());
        return productRepository.save(product);
    }

    @CacheEvict(value = {"product", "products"}, allEntries = true)
    @Override
    public Product updateProductQuantity(String token, long productId, UpdateProductQuantityDto updateProductQuantityDto) {
        validateAdminRole(token);
        Product product = getProductById(productId);
        product.setQuantity(updateProductQuantityDto.getUpdatedQuantity());
        return productRepository.save(product);
    }


    @CacheEvict(value = {"product", "products"}, allEntries = true)
    @Override
    public boolean deleteProduct(String token, long productId) {
        validateAdminRole(token);
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }

    @Override
    public int getProductStock(long productId) {
        return productRepository.getStockByProductId(productId);
    }

    @Override
    @Transactional
    public boolean reduceStock(long productId, int quantity) {
        int updatedRows = productRepository.reduceStock(productId, quantity);
        return updatedRows > 0;
    }

    @Override
    @Transactional
    public void increaseStock(long productId, int quantity) {
        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }
}
