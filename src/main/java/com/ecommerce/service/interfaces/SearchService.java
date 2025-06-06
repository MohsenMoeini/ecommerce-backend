package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SearchService {
    /**
     * Search for products based on a keyword
     * @param keyword The search term
     * @param pageable Pagination parameters
     * @return Page of matching products
     */
    Page<Product> searchProducts(String keyword, Pageable pageable);
    
    /**
     * Advanced search with multiple filters
     * @param filters Map of filter criteria (category, price range, rating, etc.)
     * @param pageable Pagination parameters
     * @return Page of matching products
     */
    Page<Product> advancedSearch(Map<String, Object> filters, Pageable pageable);
    
    /**
     * Get personalized product recommendations for a user
     * @param userId The user ID to get recommendations for
     * @param limit Maximum number of recommendations to return
     * @return List of recommended products
     */
    List<Product> getPersonalizedRecommendations(Long userId, int limit);
    
    /**
     * Get trending products based on recent orders and views
     * @param limit Maximum number of products to return
     * @return List of trending products
     */
    List<Product> getTrendingProducts(int limit);
    
    /**
     * Get related products based on a given product ID
     * @param productId Reference product ID
     * @param limit Maximum number of related products to return
     * @return List of related products
     */
    List<Product> getRelatedProducts(Long productId, int limit);
}
