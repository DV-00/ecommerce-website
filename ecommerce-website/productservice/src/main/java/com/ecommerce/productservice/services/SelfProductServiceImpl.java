package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service("selfproduct")
public class SelfProductServiceImpl implements ProductService {

    private final ProductService productService;
    private ProductRepository productRepository;
    private CategoryService categoryService;

    @Autowired
    public SelfProductServiceImpl(ProductRepository productRepository, CategoryService categoryService, ProductService productService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.productService = productService;
    }


    @Override
    public Product getProductById(long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(String title, String descrption, String image, double price, String categoryName) {

        Category category = categoryService.createCategory(categoryName);
        Product product = new Product();
        product.setCategory(category);
        product.setTitle(title);
        product.setPrice(price);
        product.setImage(image);
        product.setDescription(descrption);

        return productRepository.save(product);

    }

    @Override
    public Product updateProductPrice(long productId, double updatedPrice) {
        Optional<Product> checkProduct = productRepository.findById(productId);
        if (checkProduct.isPresent()) {
            Product product = checkProduct.get();
            product.setPrice(updatedPrice);

            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public Product updateProductImage(long productId, String updatedImage) {
        Optional<Product> checkProduct = productRepository.findById(productId);
        if (checkProduct.isPresent()) {
            Product product = checkProduct.get();
            product.setImage(updatedImage);

            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public boolean deleteProduct(long productId) {
        if(productRepository.existsById(productId)){
            productRepository.deleteById(productId);
            return true;
        }
        return false;
    }
}
