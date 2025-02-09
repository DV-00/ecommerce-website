package com.ecommerce.productservice.controllers;

import com.ecommerce.productservice.dtos.CreateProductRequestDto;
import com.ecommerce.productservice.dtos.UpdateProductImageDto;
import com.ecommerce.productservice.dtos.UpdateProductPriceDto;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.services.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    // Uses the "fakestore" implementation of ProductService.
    // Change to "selfproduct" to use the local database instead.
    public ProductController(@Qualifier("selfproduct") ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable("id") long id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }

    @PostMapping("")
    public Product createProduct (@RequestBody CreateProductRequestDto requestDto) {
        return productService.createProduct(requestDto.getTitle(),
                requestDto.getDescription(), requestDto.getImage(),
                requestDto.getPrice(),requestDto.getCategoryName());

    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updateProductPrice(@PathVariable("id") long productId, @RequestBody UpdateProductPriceDto dto) {

        Product updatedProductPrice = productService.updateProductPrice(productId, dto.getUpdatedPrice());
        return ResponseEntity.ok(updatedProductPrice);
    }

    @PatchMapping("/{id}/image")
    public ResponseEntity<Product> updateProductImage(@PathVariable("id") long productId, @RequestBody UpdateProductImageDto dto) {

        Product updatedProductImage = productService.updateProductImage(productId, dto.getUpdatedImage());
        return ResponseEntity.ok(updatedProductImage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") long productId) {
        boolean productDeleted = productService.deleteProduct(productId);
        if (productDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
