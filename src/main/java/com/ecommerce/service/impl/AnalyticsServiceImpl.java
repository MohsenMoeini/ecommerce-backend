package com.ecommerce.service.impl;

import com.ecommerce.repository.stats.InventoryStatsRepository;
import com.ecommerce.repository.stats.OrderStatsRepository;
import com.ecommerce.repository.stats.ProductStatsRepository;
import com.ecommerce.repository.stats.UserActivityRepository;
import com.ecommerce.service.interfaces.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderStatsRepository orderStatsRepository;
    private final ProductStatsRepository productStatsRepository;
    private final InventoryStatsRepository inventoryStatsRepository;
    private final UserActivityRepository userActivityRepository;

    @Override
    public Map<String, Object> getOrderStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = orderStatsRepository.getOrderStatsByDateRange(startDate, endDate);
        Map<String, Object> stats = new HashMap<>();
        
        if (!results.isEmpty()) {
            Object[] data = results.get(0);
            stats.put("orderCount", data[0]);
            stats.put("totalRevenue", data[1]);
            stats.put("averageOrderValue", data[2]);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getMonthlyOrderStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = orderStatsRepository.getMonthlyOrderStats(startDate, endDate);
        List<Map<String, Object>> stats = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", data[0]);
            entry.put("orderCount", data[1]);
            entry.put("revenue", data[2]);
            stats.add(entry);
        }
        
        return stats;
    }

    @Override
    public Map<String, Long> getOrderCountByStatus() {
        List<Object[]> results = orderStatsRepository.getOrderCountByStatus();
        Map<String, Long> stats = new HashMap<>();
        
        for (Object[] data : results) {
            stats.put((String) data[0], (Long) data[1]);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getTopCustomersByRevenue(int limit) {
        List<Object[]> results = orderStatsRepository.findTopCustomersByRevenue(limit);
        List<Map<String, Object>> customers = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("userId", data[0]);
            customer.put("email", data[1]);
            customer.put("orderCount", data[2]);
            customer.put("totalSpent", data[3]);
            customers.add(customer);
        }
        
        return customers;
    }

    @Override
    public List<Map<String, Object>> getProductStatsByCategory() {
        List<Object[]> results = productStatsRepository.getProductStatsByCategory();
        List<Map<String, Object>> stats = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("category", data[0]);
            entry.put("productCount", data[1]);
            entry.put("averagePrice", data[2]);
            stats.add(entry);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Object[]> results = productStatsRepository.findTopSellingProducts(startDate, endDate, limit);
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", data[0]);
            product.put("name", data[1]);
            product.put("totalSold", data[2]);
            products.add(product);
        }
        
        return products;
    }

    @Override
    public List<Map<String, Object>> getCategoryRevenueInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = productStatsRepository.getCategoryRevenueInPeriod(startDate, endDate);
        List<Map<String, Object>> categoryRevenue = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("category", data[0]);
            entry.put("revenue", data[1]);
            categoryRevenue.add(entry);
        }
        
        return categoryRevenue;
    }

    @Override
    public List<Map<String, Object>> getTopRatedProducts(int minReviews, int limit) {
        List<Object[]> results = productStatsRepository.findTopRatedProducts(minReviews, limit);
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", data[0]);
            product.put("name", data[1]);
            product.put("averageRating", data[2]);
            product.put("reviewCount", data[3]);
            products.add(product);
        }
        
        return products;
    }

    @Override
    public List<Map<String, Object>> getLowStockProducts(int lowStockThreshold) {
        List<Object[]> results = productStatsRepository.findLowStockProducts(lowStockThreshold);
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", data[0]);
            product.put("name", data[1]);
            product.put("lowStockCount", data[2]);
            products.add(product);
        }
        
        return products;
    }

    @Override
    public List<Map<String, Object>> getInventorySummaryByWarehouse() {
        List<Object[]> results = inventoryStatsRepository.getInventorySummaryByWarehouse();
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("warehouseName", data[0]);
            entry.put("productCount", data[1]);
            entry.put("totalQuantity", data[2]);
            summary.add(entry);
        }
        
        return summary;
    }

    @Override
    public List<Map<String, Object>> getProductStockSummary() {
        List<Object[]> results = inventoryStatsRepository.getProductStockSummary();
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("productName", data[0]);
            entry.put("totalStock", data[1]);
            entry.put("totalReserved", data[2]);
            summary.add(entry);
        }
        
        return summary;
    }

    @Override
    public List<Map<String, Object>> getWarehouseValueSummary() {
        List<Object[]> results = inventoryStatsRepository.getWarehouseValueSummary();
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("warehouseName", data[0]);
            entry.put("location", data[1]);
            entry.put("productCount", data[2]);
            entry.put("totalQuantity", data[3]);
            entry.put("totalValue", data[4]);
            summary.add(entry);
        }
        
        return summary;
    }

    @Override
    public List<Map<String, Object>> getInventoryValueByCategory() {
        List<Object[]> results = inventoryStatsRepository.getInventoryValueByCategory();
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("category", data[0]);
            entry.put("quantity", data[1]);
            entry.put("inventoryValue", data[2]);
            summary.add(entry);
        }
        
        return summary;
    }

    @Override
    public List<Map<String, Object>> getStockShortageRisk() {
        List<Object[]> results = inventoryStatsRepository.findStockShortageRisk();
        List<Map<String, Object>> risks = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("productId", data[0]);
            entry.put("productName", data[1]);
            entry.put("availableStock", data[2]);
            entry.put("reservedStock", data[3]);
            entry.put("last30DaysSales", data[4]);
            risks.add(entry);
        }
        
        return risks;
    }

    @Override
    public List<Map<String, Object>> getMonthlyActiveUsers(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = userActivityRepository.getMonthlyActiveUsers(startDate, endDate);
        List<Map<String, Object>> activeUsers = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", data[0]);
            entry.put("activeUsers", data[1]);
            activeUsers.add(entry);
        }
        
        return activeUsers;
    }

    @Override
    public List<Map<String, Object>> getInactiveCustomers(int inactiveDays) {
        List<Object[]> results = userActivityRepository.findInactiveCustomers(inactiveDays);
        List<Map<String, Object>> inactiveCustomers = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("userId", data[0]);
            customer.put("email", data[1]);
            customer.put("firstName", data[2]);
            customer.put("lastName", data[3]);
            customer.put("lastOrderDate", data[4]);
            customer.put("totalOrders", data[5]);
            customer.put("lifetimeValue", data[6]);
            inactiveCustomers.add(customer);
        }
        
        return inactiveCustomers;
    }

    @Override
    public List<Map<String, Object>> getUserSegmentAnalytics() {
        List<Object[]> results = userActivityRepository.getUserSegmentAnalytics();
        List<Map<String, Object>> segments = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> segment = new HashMap<>();
            segment.put("segment", data[0]);
            segment.put("userCount", data[1]);
            segment.put("orderCount", data[2]);
            segment.put("averageOrderValue", data[3]);
            segments.add(segment);
        }
        
        return segments;
    }

    @Override
    public List<Map<String, Object>> getTopUserCategories(Long userId) {
        List<Object[]> results = userActivityRepository.getTopUserCategories(userId);
        List<Map<String, Object>> categories = new ArrayList<>();
        
        for (Object[] data : results) {
            Map<String, Object> category = new HashMap<>();
            category.put("categoryId", data[0]);
            category.put("categoryName", data[1]);
            category.put("uniqueUsers", data[2]);
            categories.add(category);
        }
        
        return categories;
    }
}
