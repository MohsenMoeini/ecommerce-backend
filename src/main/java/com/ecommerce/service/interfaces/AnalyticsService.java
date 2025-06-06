package com.ecommerce.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    // Order analytics
    Map<String, Object> getOrderStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Map<String, Object>> getMonthlyOrderStats(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getOrderCountByStatus();
    List<Map<String, Object>> getTopCustomersByRevenue(int limit);
    
    // Product analytics
    List<Map<String, Object>> getProductStatsByCategory();
    List<Map<String, Object>> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit);
    List<Map<String, Object>> getCategoryRevenueInPeriod(LocalDateTime startDate, LocalDateTime endDate);
    List<Map<String, Object>> getTopRatedProducts(int minReviews, int limit);
    List<Map<String, Object>> getLowStockProducts(int lowStockThreshold);
    
    // Inventory analytics
    List<Map<String, Object>> getInventorySummaryByWarehouse();
    List<Map<String, Object>> getProductStockSummary();
    List<Map<String, Object>> getWarehouseValueSummary();
    List<Map<String, Object>> getInventoryValueByCategory();
    List<Map<String, Object>> getStockShortageRisk();
    
    // User analytics
    List<Map<String, Object>> getMonthlyActiveUsers(LocalDateTime startDate, LocalDateTime endDate);
    List<Map<String, Object>> getInactiveCustomers(int inactiveDays);
    List<Map<String, Object>> getUserSegmentAnalytics();
    List<Map<String, Object>> getTopUserCategories(Long userId);
}
