package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Product;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface FakeStoreProductService {

    Mono<Product> getProductById(long id);

    Flux<Product> getAllProducts();

}
