package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(Long categoryId, Category category);
    Optional<Category> getCategoryById(Long categoryId);
    Optional<Category> getCategoryByName(String name);
    List<Category> getAllCategories();
    List<Category> getSubcategories(Long parentCategoryId);
    void deleteCategory(Long categoryId);
}
