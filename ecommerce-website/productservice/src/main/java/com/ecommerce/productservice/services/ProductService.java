package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Product;
import java.util.List;

public interface ProductService {

    public Product getProductById(long id);

    public List<Product> getAllProducts();
}
