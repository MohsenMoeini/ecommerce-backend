package com.ecommerce.repository.stats;

import com.ecommerce.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryStatsRepository extends JpaRepository<Inventory, Long> {

    @Query("SELECT w.name, COUNT(i), SUM(i.quantity) FROM Inventory i " +
           "JOIN i.warehouse w GROUP BY w.name")
    List<Object[]> getInventorySummaryByWarehouse();
    
    @Query(value = "SELECT p.name, SUM(i.quantity) as total_stock, " +
           "SUM(i.quantity_reserved) as total_reserved " +
           "FROM inventory i JOIN product p ON i.product_id = p.id " +
           "GROUP BY p.id ORDER BY total_stock DESC",
           nativeQuery = true)
    List<Object[]> getProductStockSummary();
    
    @Query(value = "SELECT w.name as warehouse, w.location, COUNT(i.id) as product_count, " +
           "SUM(i.quantity) as total_quantity, " +
           "SUM(i.quantity * p.price) as total_value " +
           "FROM inventory i " +
           "JOIN warehouse w ON i.warehouse_id = w.id " +
           "JOIN product p ON i.product_id = p.id " +
           "GROUP BY w.id ORDER BY total_value DESC",
           nativeQuery = true)
    List<Object[]> getWarehouseValueSummary();
    
    @Query(value = "SELECT c.name as category, SUM(i.quantity) as quantity, " +
           "SUM(i.quantity * p.price) as inventory_value " +
           "FROM inventory i " +
           "JOIN product p ON i.product_id = p.id " +
           "JOIN category c ON p.category_id = c.id " +
           "GROUP BY c.id ORDER BY inventory_value DESC",
           nativeQuery = true)
    List<Object[]> getInventoryValueByCategory();
    
    @Query(value = "SELECT p.id, p.name, " +
           "SUM(i.quantity) as available, " +
           "SUM(i.quantity_reserved) as reserved, " +
           "(SELECT COALESCE(SUM(oi.quantity), 0) FROM order_item oi " +
           "JOIN orders o ON oi.order_id = o.id " +
           "WHERE oi.product_id = p.id AND o.order_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)) as last_30_days_sales " +
           "FROM inventory i " +
           "JOIN product p ON i.product_id = p.id " +
           "GROUP BY p.id " +
           "HAVING available - last_30_days_sales < 0 " +
           "ORDER BY (available - last_30_days_sales)",
           nativeQuery = true) 
    List<Object[]> findStockShortageRisk();
}
