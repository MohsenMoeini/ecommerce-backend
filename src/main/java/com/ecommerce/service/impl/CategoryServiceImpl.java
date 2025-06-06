package com.ecommerce.service.impl;

import com.ecommerce.entity.Category;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.interfaces.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Check if category with this name already exists
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long categoryId, Category category) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        
        // Check if name is being changed and if new name already exists
        if (!existingCategory.getName().equals(category.getName()) && 
            categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setParent(category.getParent());
        
        return categoryRepository.save(existingCategory);
    }

    @Override
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getSubcategories(Long parentCategoryId) {
        return categoryRepository.findByParentCategoryId(parentCategoryId);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        // Check if category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId);
        }
        
        // Check if category has subcategories
        List<Category> subcategories = categoryRepository.findByParentCategoryId(categoryId);
        if (!subcategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category with subcategories. Delete subcategories first.");
        }
        
        categoryRepository.deleteById(categoryId);
    }
}
