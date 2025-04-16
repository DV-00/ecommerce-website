package com.ecommerce.productservice.repositories;

import com.ecommerce.productservice.models.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testFindByName() {
        Category category = new Category();
        category.setName("Test Category");
        categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findByName("Test Category");

        assertTrue(found.isPresent());
        assertEquals("Test Category", found.get().getName());
    }
}
