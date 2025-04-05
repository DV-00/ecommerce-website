package com.ecommerce.productservice.services;

import com.ecommerce.productservice.models.Category;
import com.ecommerce.productservice.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCategory_NewCategory() {
        String categoryName = "Electronics";
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.empty());

        Category category = new Category();
        category.setName(categoryName);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category createdCategory = categoryService.createCategory(categoryName);

        assertNotNull(createdCategory);
        assertEquals(categoryName, createdCategory.getName());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
