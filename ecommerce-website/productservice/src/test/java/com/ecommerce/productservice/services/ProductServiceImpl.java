package com.ecommerce.productservice.services;

import com.ecommerce.productservice.dtos.*;
import com.ecommerce.productservice.exceptions.UnauthorizedException;
import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.repositories.CategoryRepository;
import com.ecommerce.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Primary
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final WebClient webClient;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              WebClient.Builder webClientBuilder,
                              RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.redisTemplate = redisTemplate;
        this.webClient = webClientBuilder.baseUrl("http://userservice:8080/").build();
    }

    // ------------------ HELPER ------------------
    private void validateAdminRole(String token) {
        try {
            UserResponseDto userDto = webClient.get()
                    .uri("/users/validate?token=" + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        throw new UnauthorizedException("Invalid token or access denied.");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        throw new RuntimeException("User Service is unavailable. Please try again later.");
                    })
                    .bodyToMono(UserResponseDto.class)
                    .block();

            if (userDto == null || !"ADMIN".equals(userDto.getRole())) {
                throw new UnauthorizedException("Access denied! Only admins can perform this action.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnauthorizedException("Invalid token or access denied.");
        }
    }

    // ------------------ READ ------------------
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        String key = "products:page:" + pageable.getPageNumber();
        Page<Product> cachedPage = (Page<Product>) redisTemplate.opsForValue().get(key);
        if (cachedPage != null) {
            return cachedPage;
        }

        Page<Product> page = productRepository.findAll(pageable);
        redisTemplate.opsForValue().set(key, page);
        return page;
    }

    @Override
    public Product getProductById(long id) {
        String key = "product:" + id;
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(key);
        if (cachedProduct != null) {
            return cachedProduct;
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        redisTemplate.opsForValue().set(key, product);
        return product;
    }

    @Override
    public int getProductStock(long productId) {
        return productRepository.getStockByProductId(productId);
    }

    // ------------------ CREATE ------------------
    @Override
    public Product createProduct(String token, CreateProductRequestDto dto) {
        try {
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

            Product saved = productRepository.save(product);
            redisTemplate.opsForValue().set("product:" + saved.getId(), saved);
            return saved;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------ UPDATE ------------------
    @Override
    public Product updateProductPrice(String token, long productId, UpdateProductPriceDto dto) {
        try {
            validateAdminRole(token);
            Product product = getProductById(productId);
            product.setPrice(dto.getUpdatedPrice());
            Product updated = productRepository.save(product);
            redisTemplate.opsForValue().set("product:" + productId, updated);
            return updated;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Product updateProductImage(String token, long productId, UpdateProductImageDto dto) {
        try {
            validateAdminRole(token);
            Product product = getProductById(productId);
            product.setImage(dto.getUpdatedImage());
            Product updated = productRepository.save(product);
            redisTemplate.opsForValue().set("product:" + productId, updated);
            return updated;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Product updateProductQuantity(String token, long productId, UpdateProductQuantityDto dto) {
        try {
            validateAdminRole(token);
            Product product = getProductById(productId);
            product.setQuantity(dto.getUpdatedQuantity());
            Product updated = productRepository.save(product);
            redisTemplate.opsForValue().set("product:" + productId, updated);
            return updated;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------ STOCK ------------------
    @Override
    @Transactional
    public boolean reduceStock(long productId, int quantity) {
        try {
            int updatedRows = productRepository.reduceStock(productId, quantity);
            if (updatedRows > 0) {
                redisTemplate.delete("product:" + productId);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseStock(long productId, int quantity) {
        try {
            Product product = getProductById(productId);
            product.setQuantity(product.getQuantity() + quantity);
            Product updated = productRepository.save(product);
            redisTemplate.opsForValue().set("product:" + productId, updated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restoreStock(Long productId, Integer quantity) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setQuantity(product.getQuantity() + quantity);
                Product updated = productRepository.save(product);
                redisTemplate.opsForValue().set("product:" + productId, updated);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------ DELETE ------------------
    @Override
    public boolean deleteProduct(String token, long productId) {
        try {
            validateAdminRole(token);
            if (productRepository.existsById(productId)) {
                productRepository.deleteById(productId);
                redisTemplate.delete("product:" + productId);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
