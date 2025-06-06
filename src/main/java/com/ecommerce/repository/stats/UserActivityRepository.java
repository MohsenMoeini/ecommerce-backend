package com.ecommerce.repository.stats;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT DATE_FORMAT(o.order_date, '%Y-%m') as month, " +
           "COUNT(DISTINCT o.user_id) as active_users " +
           "FROM orders o " +
           "WHERE o.order_date BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE_FORMAT(o.order_date, '%Y-%m') " +
           "ORDER BY month",
           nativeQuery = true)
    List<Object[]> getMonthlyActiveUsers(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query(value = "SELECT u.id, u.email, u.first_name, u.last_name, " +
           "MAX(o.order_date) as last_order_date, " +
           "COUNT(o.id) as total_orders, " +
           "SUM(o.total_amount) as lifetime_value " +
           "FROM user u JOIN orders o ON u.id = o.user_id " +
           "GROUP BY u.id " +
           "HAVING MAX(o.order_date) < DATE_SUB(NOW(), INTERVAL :inactiveDays DAY) " +
           "ORDER BY last_order_date",
           nativeQuery = true)
    List<Object[]> findInactiveCustomers(int inactiveDays);
    
    @Query(value = "SELECT " +
           "CASE " +
           "  WHEN DATEDIFF(NOW(), u.created_at) <= 30 THEN 'New (0-30 days)' " +
           "  WHEN DATEDIFF(NOW(), u.created_at) <= 90 THEN 'Recent (31-90 days)' " +
           "  WHEN DATEDIFF(NOW(), u.created_at) <= 365 THEN 'Established (91-365 days)' " +
           "  ELSE 'Loyal (365+ days)' " +
           "END as customer_segment, " +
           "COUNT(DISTINCT u.id) as user_count, " +
           "COUNT(o.id) as order_count, " +
           "AVG(o.total_amount) as avg_order_value " +
           "FROM user u " +
           "LEFT JOIN orders o ON u.id = o.user_id " +
           "GROUP BY customer_segment " +
           "ORDER BY customer_segment",
           nativeQuery = true)
    List<Object[]> getUserSegmentAnalytics();
    
    @Query(value = "SELECT p.category_id, c.name as category_name, " +
           "COUNT(DISTINCT r.user_id) as unique_users " +
           "FROM review r " +
           "JOIN product p ON r.product_id = p.id " +
           "JOIN category c ON p.category_id = c.id " +
           "WHERE r.user_id = :userId " +
           "GROUP BY p.category_id " +
           "ORDER BY unique_users DESC " +
           "LIMIT 3",
           nativeQuery = true)
    List<Object[]> getTopUserCategories(Long userId);
}
