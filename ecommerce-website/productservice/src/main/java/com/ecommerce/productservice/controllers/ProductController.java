package com.ecommerce.productservice.controllers;

import com.ecommerce.productservice.dtos.CreateProductRequestDto;
import com.ecommerce.productservice.dtos.UpdateProductImageDto;
import com.ecommerce.productservice.dtos.UpdateProductPriceDto;
import com.ecommerce.productservice.dtos.UpdateProductQuantityDto;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate; // Add this line

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestHeader("Authorization") String token,
                                                 @Valid @RequestBody CreateProductRequestDto createProductRequestDto) {
        return ResponseEntity.ok(productService.createProduct(token, createProductRequestDto));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updateProductPrice(@RequestHeader("Authorization") String token,
                                                      @PathVariable("id") long id,
                                                      @Valid @RequestBody UpdateProductPriceDto updateProductPriceDto) {
        return ResponseEntity.ok(productService.updateProductPrice(token, id, updateProductPriceDto));
    }

    @PatchMapping("/{id}/image")
    public ResponseEntity<Product> updateProductImage(@RequestHeader("Authorization") String token,
                                                      @PathVariable("id") long id,
                                                      @Valid @RequestBody UpdateProductImageDto updateProductImageDto) {
        return ResponseEntity.ok(productService.updateProductImage(token, id, updateProductImageDto));
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Product> updateProductQuantity(@RequestHeader("Authorization") String token,
                                                         @PathVariable("id") long id,
                                                         @Valid @RequestBody UpdateProductQuantityDto updateProductQuantityDto) {
        return ResponseEntity.ok(productService.updateProductQuantity(token, id, updateProductQuantityDto));
    }

    @PatchMapping("/{id}/increase-stock/{quantity}")
    public ResponseEntity<Void> increaseStock(@PathVariable long id, @PathVariable int quantity) {
        productService.increaseStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reduce-stock/{quantity}")
    public ResponseEntity<Void> reduceStock(@PathVariable long id, @PathVariable int quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<Integer> getStockByProductId(@PathVariable long id) {
        return ResponseEntity.ok(productService.getProductStock(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@RequestHeader("Authorization") String token,
                                              @PathVariable("id") long id) {
        productService.deleteProduct(token, id);
        return ResponseEntity.noContent().build();
    }
}
