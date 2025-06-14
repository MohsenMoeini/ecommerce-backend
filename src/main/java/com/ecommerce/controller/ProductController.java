package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.service.interfaces.ProductService;
import com.ecommerce.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            ApiResponse<Product> errorResponse = ApiResponse.error(
                "Product not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Product.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(productOpt.get()));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Product>>> searchProducts(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Product> products = searchService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<ApiResponse<Page<Product>>> advancedSearch(
            @RequestBody Map<String, Object> filters,
            Pageable pageable) {
        Page<Product> products = searchService.advancedSearch(filters, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            Pageable pageable) {
        Page<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse<List<Product>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Product>>> getTrendingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = searchService.getTrendingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<Product>>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            ApiResponse<List<Product>> errorResponse = ApiResponse.error(
                "Product not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                (Class<List<Product>>) (Class<?>) List.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        List<Product> relatedProducts = searchService.getRelatedProducts(id, limit);
        return ResponseEntity.ok(ApiResponse.success(relatedProducts));
    }
    
    @GetMapping("/recommendations/user/{userId}")
    public ResponseEntity<ApiResponse<List<Product>>> getPersonalizedRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> recommendations = searchService.getPersonalizedRecommendations(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdProduct, "Product created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            ApiResponse<Product> errorResponse = ApiResponse.error(
                "Product not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Product.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        product.setId(id);
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Product>> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            ApiResponse<Product> errorResponse = ApiResponse.error(
                "Product not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Product.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Product updatedProduct = productService.updateProductStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product stock updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}
