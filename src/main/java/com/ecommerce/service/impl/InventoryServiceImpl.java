package com.ecommerce.service.impl;

import com.ecommerce.entity.Inventory;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.service.interfaces.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public Inventory createInventory(Inventory inventory) {
        // Check if inventory already exists for this product and warehouse
        Optional<Inventory> existingInventory = 
                inventoryRepository.findByProductIdAndWarehouseId(
                        inventory.getProduct().getId(), 
                        inventory.getWarehouse().getId());
        
        if (existingInventory.isPresent()) {
            throw new RuntimeException("Inventory already exists for this product and warehouse");
        }
        
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory updateInventory(Long inventoryId, Inventory inventory) {
        Inventory existingInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + inventoryId));
        
        // Update inventory fields
        existingInventory.setQuantity(inventory.getQuantity());
        existingInventory.setReservedQuantity(inventory.getReservedQuantity());
        existingInventory.setReorderThreshold(inventory.getReorderThreshold());
        existingInventory.setReorderQuantity(inventory.getReorderQuantity());
        existingInventory.setStatus(inventory.getStatus());
        existingInventory.setSku(inventory.getSku());
        existingInventory.setBatchNumber(inventory.getBatchNumber());
        existingInventory.setExpiryDate(inventory.getExpiryDate());
        
        return inventoryRepository.save(existingInventory);
    }

    @Override
    public Optional<Inventory> getInventoryById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    public List<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    @Override
    public List<Inventory> getInventoryByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    @Override
    public Optional<Inventory> getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Override
    @Transactional
    public void deleteInventory(Long inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new EntityNotFoundException("Inventory not found with id: " + inventoryId);
        }
        inventoryRepository.deleteById(inventoryId);
    }

    @Override
    @Transactional
    public Inventory adjustInventoryQuantity(Long inventoryId, int quantityChange) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + inventoryId));
        
        int newQuantity = inventory.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Cannot reduce quantity below zero");
        }
        
        inventory.setQuantity(newQuantity);
        
        // Update status based on new quantity and reorder threshold
        updateInventoryStatus(inventory);
        
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory reserveInventory(Long inventoryId, int quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + inventoryId));
        
        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient inventory available");
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        
        // Update status based on available quantity
        updateInventoryStatus(inventory);
        
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory releaseReservedInventory(Long inventoryId, int quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + inventoryId));
        
        if (inventory.getReservedQuantity() < quantity) {
            throw new RuntimeException("Cannot release more than reserved quantity");
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        
        // Update status based on available quantity
        updateInventoryStatus(inventory);
        
        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> getLowStockInventory() {
        return getAllInventory().stream()
                .filter(inv -> inv.isLowStock())
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to update inventory status based on quantity and threshold
     */
    private void updateInventoryStatus(Inventory inventory) {
        if (inventory.getQuantity() <= 0) {
            inventory.setStatus(Inventory.InventoryStatus.OUT_OF_STOCK);
        } else if (inventory.isLowStock()) {
            inventory.setStatus(Inventory.InventoryStatus.LOW_STOCK);
        } else {
            inventory.setStatus(Inventory.InventoryStatus.AVAILABLE);
        }
    }
}
