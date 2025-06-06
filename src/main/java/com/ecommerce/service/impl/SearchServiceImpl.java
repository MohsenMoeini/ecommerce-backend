package com.ecommerce.service.impl;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        // Basic search using the repository method
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Page<Product> advancedSearch(Map<String, Object> filters, Pageable pageable) {
        String keyword = filters.containsKey("keyword") ? (String) filters.get("keyword") : null;
        Long categoryId = filters.containsKey("category") ? Long.valueOf(filters.get("category").toString()) : null;
        Double minPrice = filters.containsKey("minPrice") ? Double.valueOf(filters.get("minPrice").toString()) : 0.0;
        Double maxPrice = filters.containsKey("maxPrice") ? Double.valueOf(filters.get("maxPrice").toString()) : Double.MAX_VALUE;
        Integer minRating = filters.containsKey("minRating") ? Integer.valueOf(filters.get("minRating").toString()) : null;
        
        // Use the custom repository implementation with criteria API
        return productRepository.findByAdvancedFilters(keyword, categoryId, minPrice, maxPrice, minRating, pageable);
    }

    @Override
    public List<Product> getPersonalizedRecommendations(Long userId, int limit) {
        // In a real implementation, this would use user history and preferences
        // For now, we'll return top-rated products as a fallback
        return productRepository.findByHighestRating(Pageable.ofSize(limit)).getContent();
    }

    @Override
    public List<Product> getTrendingProducts(int limit) {
        // Get trending products based on sales count
        return productRepository.findTop5ByOrderBySalesCountDesc();
    }

    @Override
    public List<Product> getRelatedProducts(Long productId, int limit) {
        // Get related products based on category, tags, or other attributes
        return productRepository.findRelatedProducts(productId, limit);
    }
}
