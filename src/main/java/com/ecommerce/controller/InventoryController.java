package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Inventory;
import com.ecommerce.service.interfaces.InventoryService;
import com.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Inventory>>> getAllInventory() {
        List<Inventory> inventoryItems = inventoryService.getAllInventory();
        return ResponseEntity.ok(ApiResponse.success(inventoryItems));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Inventory>> getInventoryById(@PathVariable Long id) {
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryById(id);
        if (inventoryOpt.isEmpty()) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error("Inventory not found with id: " + id, HttpStatus.NOT_FOUND.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(inventoryOpt.get()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Inventory>>> getInventoryByProductId(@PathVariable Long productId) {
        List<Inventory> inventoryItems = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(inventoryItems));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Inventory>>> getInventoryByWarehouseId(@PathVariable Long warehouseId) {
        List<Inventory> inventoryItems = inventoryService.getInventoryByWarehouseId(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(inventoryItems));
    }
    
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Inventory>> getInventoryByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryByProductAndWarehouse(productId, warehouseId);
        if (inventoryOpt.isEmpty()) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error(
                "Inventory not found for product id: " + productId + " and warehouse id: " + warehouseId, 
                HttpStatus.NOT_FOUND.value(), 
                Inventory.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(inventoryOpt.get()));
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Inventory>>> getLowStockInventory() {
        List<Inventory> lowStockItems = inventoryService.getLowStockInventory();
        return ResponseEntity.ok(ApiResponse.success(lowStockItems));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Inventory>> createInventory(@Valid @RequestBody Inventory inventory) {
        Inventory createdInventory = inventoryService.createInventory(inventory);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdInventory, "Inventory created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Inventory>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody Inventory inventory) {
        
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryById(id);
        if (inventoryOpt.isEmpty()) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error("Inventory not found with id: " + id, HttpStatus.NOT_FOUND.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        inventory.setId(id);
        Inventory updatedInventory = inventoryService.updateInventory(id, inventory);
        return ResponseEntity.ok(ApiResponse.success(updatedInventory, "Inventory updated successfully"));
    }

    @PutMapping("/{id}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Inventory>> adjustInventoryQuantity(
            @PathVariable Long id,
            @RequestParam int quantityChange) {
        
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryById(id);
        if (inventoryOpt.isEmpty()) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error("Inventory not found with id: " + id, HttpStatus.NOT_FOUND.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Inventory updatedInventory = inventoryService.adjustInventoryQuantity(id, quantityChange);
        return ResponseEntity.ok(ApiResponse.success(updatedInventory, "Inventory quantity adjusted successfully"));
    }
    
    @PutMapping("/{id}/reserve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Inventory>> reserveInventory(
            @PathVariable Long id,
            @RequestParam int quantity) {
        
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryById(id);
        if (inventoryOpt.isEmpty()) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error("Inventory not found with id: " + id, HttpStatus.NOT_FOUND.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            Inventory updatedInventory = inventoryService.reserveInventory(id, quantity);
            return ResponseEntity.ok(ApiResponse.success(updatedInventory, "Inventory reserved successfully"));
        } catch (IllegalArgumentException e) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Inventory>> releaseReservedInventory(
            @PathVariable Long id,
            @RequestParam int quantity) {
        
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryById(id);
        if (inventoryOpt.isEmpty()) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error("Inventory not found with id: " + id, HttpStatus.NOT_FOUND.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            Inventory updatedInventory = inventoryService.releaseReservedInventory(id, quantity);
            return ResponseEntity.ok(ApiResponse.success(updatedInventory, "Reserved inventory released successfully"));
        } catch (IllegalArgumentException e) {
            ApiResponse<Inventory> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Inventory.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        Optional<Inventory> inventoryOpt = inventoryService.getInventoryById(id);
        if (inventoryOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Inventory not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory deleted successfully"));
    }
}
