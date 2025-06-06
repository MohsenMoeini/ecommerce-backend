package com.ecommerce.service.impl;

import com.ecommerce.entity.Warehouse;
import com.ecommerce.repository.WarehouseRepository;
import com.ecommerce.service.interfaces.WarehouseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        // Check if a warehouse with this name already exists
        if (warehouseRepository.existsByName(warehouse.getName())) {
            throw new RuntimeException("Warehouse with name '" + warehouse.getName() + "' already exists");
        }
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouse) {
        Warehouse existingWarehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with id: " + warehouseId));
        
        // Check if name is being changed and if new name already exists
        if (!existingWarehouse.getName().equals(warehouse.getName()) &&
            warehouseRepository.existsByName(warehouse.getName())) {
            throw new RuntimeException("Warehouse with name '" + warehouse.getName() + "' already exists");
        }
        
        // Update fields
        existingWarehouse.setName(warehouse.getName());
        existingWarehouse.setCode(warehouse.getCode());
        existingWarehouse.setDescription(warehouse.getDescription());
        existingWarehouse.setActive(warehouse.isActive());
        existingWarehouse.setAddress(warehouse.getAddress());
        existingWarehouse.setPhoneNumber(warehouse.getPhoneNumber());
        existingWarehouse.setEmail(warehouse.getEmail());
        existingWarehouse.setManagerName(warehouse.getManagerName());
        
        return warehouseRepository.save(existingWarehouse);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new EntityNotFoundException("Warehouse not found with id: " + warehouseId);
        }
        warehouseRepository.deleteById(warehouseId);
    }

    @Override
    public Optional<Warehouse> getWarehouseById(Long warehouseId) {
        return warehouseRepository.findById(warehouseId);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @Override
    public List<Warehouse> getActiveWarehouses() {
        return warehouseRepository.findByActiveTrue();
    }

    @Override
    public List<Warehouse> findWarehousesByProximity(double latitude, double longitude, double radiusInKm) {
        // In a real application, this would use geospatial queries
        // For now, we'll just return all warehouses as a placeholder
        return warehouseRepository.findAll();
    }
}
