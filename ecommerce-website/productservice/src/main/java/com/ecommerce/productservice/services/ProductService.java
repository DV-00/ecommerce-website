package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Product;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.List;

public interface ProductService {

    Mono<Product> getProductById(long id);

    Flux<Product> getAllProducts();

}
