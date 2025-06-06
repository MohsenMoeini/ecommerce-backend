package com.ecommerce.repository.stats;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductStatsRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p.category.name, COUNT(p), AVG(p.price) FROM Product p GROUP BY p.category.name")
    List<Object[]> getProductStatsByCategory();
    
    @Query(value = "SELECT p.id, p.name, SUM(oi.quantity) as total_sold " +
           "FROM product p JOIN order_item oi ON p.id = oi.product_id " +
           "JOIN orders o ON oi.order_id = o.id " +
           "WHERE o.order_date BETWEEN :startDate AND :endDate " +
           "GROUP BY p.id ORDER BY total_sold DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    @Query(value = "SELECT c.name as category, SUM(oi.quantity * oi.price) as revenue " +
           "FROM order_item oi " +
           "JOIN product p ON oi.product_id = p.id " +
           "JOIN category c ON p.category_id = c.id " +
           "JOIN orders o ON oi.order_id = o.id " +
           "WHERE o.order_date BETWEEN :startDate AND :endDate " +
           "GROUP BY c.name ORDER BY revenue DESC",
           nativeQuery = true)
    List<Object[]> getCategoryRevenueInPeriod(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query(value = "SELECT p.id, p.name, AVG(r.rating) as avg_rating, COUNT(r.id) as review_count " +
           "FROM product p JOIN review r ON p.id = r.product_id " +
           "GROUP BY p.id HAVING review_count >= :minReviews " +
           "ORDER BY avg_rating DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findTopRatedProducts(int minReviews, int limit);
    
    @Query(value = "SELECT p.id, p.name, " +
           "(SELECT COUNT(*) FROM inventory i WHERE i.product_id = p.id AND i.quantity_available <= :lowStockThreshold) as low_stock_count " +
           "FROM product p " +
           "WHERE (SELECT SUM(i.quantity_available) FROM inventory i WHERE i.product_id = p.id) <= :lowStockThreshold " +
           "ORDER BY low_stock_count DESC",
           nativeQuery = true)
    List<Object[]> findLowStockProducts(int lowStockThreshold);
}
