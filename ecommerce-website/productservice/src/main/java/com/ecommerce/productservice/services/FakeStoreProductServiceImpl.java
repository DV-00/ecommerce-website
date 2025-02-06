package com.ecommerce.productservice.services;

import com.ecommerce.productservice.dtos.FakeProductServiceDto;
import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class FakeStoreProductServiceImpl implements ProductService {

    private RestTemplate restTemplate;

    @Autowired
    public FakeStoreProductServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private Product convertDtoToProduct(FakeProductServiceDto dto) {
        Product product =new Product();
        product.setId(dto.getId());
        product.setTitle(dto.getTitle());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setImage(dto.getImage());
        Category category = new Category();
        category.setName(dto.getCategory());
        product.setCategory(category);
        return product;
    }

    @Override
    public Product getProductById(long id) {

        FakeProductServiceDto product = this.restTemplate.getForObject("https://fakestoreapi.com/products/" + id, FakeProductServiceDto.class);
        return convertDtoToProduct(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }
}
