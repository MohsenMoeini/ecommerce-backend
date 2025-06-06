package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            ApiResponse<Category> errorResponse = ApiResponse.error("Category not found with id: " + id, HttpStatus.NOT_FOUND.value(), Category.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(categoryOpt.get()));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Category>> getCategoryByName(@PathVariable String name) {
        Optional<Category> categoryOpt = categoryService.getCategoryByName(name);
        if (categoryOpt.isEmpty()) {
            ApiResponse<Category> errorResponse = ApiResponse.error("Category not found with name: " + name, HttpStatus.NOT_FOUND.value(), Category.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(categoryOpt.get()));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<ApiResponse<List<Category>>> getSubcategories(@PathVariable Long id) {
        List<Category> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(ApiResponse.success(subcategories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody Category category) {
        Optional<Category> existingCategory = categoryService.getCategoryByName(category.getName());
        if (existingCategory.isPresent()) {
            ApiResponse<Category> errorResponse = ApiResponse.error("Category with name '" + category.getName() + "' already exists", HttpStatus.CONFLICT.value(), Category.class);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
        
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCategory, "Category created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category) {
        
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            ApiResponse<Category> errorResponse = ApiResponse.error("Category not found with id: " + id, HttpStatus.NOT_FOUND.value(), Category.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Check if name is being changed and if it already exists
        if (!categoryOpt.get().getName().equals(category.getName())) {
            Optional<Category> existingCategory = categoryService.getCategoryByName(category.getName());
            if (existingCategory.isPresent()) {
                ApiResponse<Category> errorResponse = ApiResponse.error("Category with name '" + category.getName() + "' already exists", HttpStatus.CONFLICT.value(), Category.class);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
        }
        
        category.setId(id);
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(ApiResponse.success(updatedCategory, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Category not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
