package com.ecommerce.repository.custom;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomProductRepository {
    Page<Product> findByAdvancedFilters(String keyword, Long categoryId, 
                                        Double minPrice, Double maxPrice, 
                                        Integer minRating, Pageable pageable);
    
    List<Product> findRelatedProducts(Long productId, int limit);
    
    List<Product> findFrequentlyBoughtTogether(Long productId, int limit);
}
