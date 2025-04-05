package com.ecommerce.productservice.repositories;

import com.ecommerce.productservice.models.Product;
import com.ecommerce.productservice.models.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testFindByTitleAndCategory() {
        Optional<Category> existingCategory = categoryRepository.findByName("Mobiles");
        Category category = existingCategory.orElseGet(() -> {
            Category newCategory = new Category();
            newCategory.setName("Mobiles");
            return categoryRepository.save(newCategory);
        });

        entityManager.flush();
        entityManager.refresh(category);

        Product product = new Product();
        product.setTitle("Google Pixel");
        product.setCategory(category);
        product = productRepository.save(product);
        entityManager.flush();
        entityManager.refresh(product);

        Optional<Product> foundProduct = productRepository.findByTitleAndCategory("Google Pixel", category);
        assertTrue(foundProduct.isPresent(), "Product should be found in the category");
    }

    @Test
    void testGetStockByProductId() {
        Category category = new Category();
        category.setName("Books");
        entityManager.persist(category);
        entityManager.flush();

        Product product = new Product();
        product.setTitle("Java Programming");
        product.setCategory(category);
        product.setQuantity(50);
        entityManager.persist(product);
        entityManager.flush();

        // Get stock by product ID
        Integer stock = productRepository.getStockByProductId(product.getId());
        assertNotNull(stock);
        assertEquals(50, stock.intValue());
    }

    @Test
    void testReduceStock() {
        Category category = new Category();
        category.setName("Toys");
        entityManager.persist(category);
        entityManager.flush();

        Product product = new Product();
        product.setTitle("Toy Car");
        product.setCategory(category);
        product.setQuantity(30);
        entityManager.persist(product);
        entityManager.flush();

        // Attempt to reduce stock by 10
        int updated = productRepository.reduceStock(product.getId(), 10);
        assertEquals(1, updated);  // Expect 1 row updated

        entityManager.refresh(product);
        assertEquals(20, product.getQuantity());

        // Try reducing by 25 when stock is only 20 (should not update)
        int notUpdated = productRepository.reduceStock(product.getId(), 25);
        assertEquals(0, notUpdated); // No update should happen

        // Confirm quantity remains 20
        entityManager.refresh(product);
        assertEquals(20, product.getQuantity());
    }
}
