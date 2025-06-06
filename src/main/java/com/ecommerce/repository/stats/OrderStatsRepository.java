package com.ecommerce.repository.stats;

import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderStatsRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COUNT(o), SUM(o.totalAmount), AVG(o.totalAmount) FROM Order o " +
           "WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    List<Object[]> getOrderStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderedAt, '%Y-%m') as month, " +
           "COUNT(o) as orderCount, SUM(o.totalAmount) as revenue " +
           "FROM Order o " +
           "WHERE o.orderedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderedAt, '%Y-%m') " +
           "ORDER BY month")
    List<Object[]> getMonthlyOrderStats(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
    List<Object[]> getOrderCountByStatus();
    
    @Query(value = "SELECT u.id, u.email, COUNT(o.id) as order_count, SUM(o.total_amount) as total_spent " +
           "FROM orders o JOIN user u ON o.user_id = u.id " +
           "GROUP BY u.id ORDER BY total_spent DESC LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findTopCustomersByRevenue(int limit);
}
