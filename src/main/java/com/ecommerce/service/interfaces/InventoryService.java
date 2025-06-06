package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    Inventory createInventory(Inventory inventory);
    Inventory updateInventory(Long inventoryId, Inventory inventory);
    Optional<Inventory> getInventoryById(Long inventoryId);
    List<Inventory> getAllInventory();
    List<Inventory> getInventoryByProductId(Long productId);
    List<Inventory> getInventoryByWarehouseId(Long warehouseId);
    Optional<Inventory> getInventoryByProductAndWarehouse(Long productId, Long warehouseId);
    void deleteInventory(Long inventoryId);
    Inventory adjustInventoryQuantity(Long inventoryId, int quantityChange);
    Inventory reserveInventory(Long inventoryId, int quantity);
    Inventory releaseReservedInventory(Long inventoryId, int quantity);
    List<Inventory> getLowStockInventory();
}
