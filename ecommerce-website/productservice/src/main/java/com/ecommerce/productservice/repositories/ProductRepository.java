package com.ecommerce.productservice.repositories;

import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByTitleAndCategory(String title, Category category);

    @Query("SELECT p.quantity FROM Product p WHERE p.id = :productId")
    Integer getStockByProductId(Long productId);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity - :quantity WHERE p.id = :productId AND p.quantity >= :quantity")
    int reduceStock(Long productId, int quantity);

}
