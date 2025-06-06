package com.ecommerce.repository;

import com.ecommerce.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByWarehouseId(Long warehouseId);
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
