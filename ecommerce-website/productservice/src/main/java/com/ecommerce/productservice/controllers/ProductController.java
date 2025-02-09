package com.ecommerce.productservice.controllers;

import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.services.FakeStoreProductService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final FakeStoreProductService fakeStoreProductService;

    public ProductController(FakeStoreProductService fakeStoreProductService) {
        this.fakeStoreProductService = fakeStoreProductService;
    }

    @GetMapping("/{id}")
    public Mono<Product> getProductById(@PathVariable("id") long id) {
        return fakeStoreProductService.getProductById(id);
    }

    @GetMapping
    public Flux<Product> getProducts() {
        return fakeStoreProductService.getAllProducts();
    }
}
