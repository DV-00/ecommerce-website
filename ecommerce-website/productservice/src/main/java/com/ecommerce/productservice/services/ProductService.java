package com.ecommerce.productservice.services;

import com.ecommerce.productservice.dtos.CreateProductRequestDto;
import com.ecommerce.productservice.dtos.UpdateProductImageDto;
import com.ecommerce.productservice.dtos.UpdateProductPriceDto;
import com.ecommerce.productservice.dtos.UpdateProductQuantityDto;
import com.ecommerce.productservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {

    Product getProductById(long id);

    Page<Product> getAllProducts(Pageable pageable);

    Product createProduct(String token, CreateProductRequestDto createProductRequestDto);

    Product updateProductPrice(String token, long productId, UpdateProductPriceDto updateProductPriceDto);

    Product updateProductImage(String token, long productId, UpdateProductImageDto updateProductImageDto);

    Product updateProductQuantity(String token, long productId, UpdateProductQuantityDto updateProductQuantityDto);

    boolean deleteProduct(String token, long productId);

    int getProductStock(long productId);

    boolean reduceStock(long productId, int quantity);

    void increaseStock(long productId, int quantity);

    void restoreStock(Long productId, Integer quantity);

}
