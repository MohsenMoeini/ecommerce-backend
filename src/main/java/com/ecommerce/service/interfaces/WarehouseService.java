package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseService {
    Warehouse createWarehouse(Warehouse warehouse);
    Warehouse updateWarehouse(Long warehouseId, Warehouse warehouse);
    void deleteWarehouse(Long warehouseId);
    Optional<Warehouse> getWarehouseById(Long warehouseId);
    List<Warehouse> getAllWarehouses();
    List<Warehouse> getActiveWarehouses();
    List<Warehouse> findWarehousesByProximity(double latitude, double longitude, double radiusInKm);
}
