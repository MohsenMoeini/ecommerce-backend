package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Long productId, Product product);
    Optional<Product> getProductById(Long productId);
    List<Product> getAllProducts();
    Page<Product> getAllProducts(Pageable pageable);
    Page<Product> searchProducts(String keyword, Pageable pageable);
    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<Product> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable);
    List<Product> getTopSellingProducts(int limit);
    void deleteProduct(Long productId);
    Product updateProductStock(Long productId, Integer quantity);
}
