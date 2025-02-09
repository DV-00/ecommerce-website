package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.repositories.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ProductService {

    public Product getProductById(long id);

    public List<Product> getAllProducts();

    public Product createProduct(String title, String descrption, String image, double price, String category);

    public Product updateProductPrice(long productId, double updatedPrice);

    public Product updateProductImage(long productId, String updatedImage);

    public boolean deleteProduct(long ProductId);

}
