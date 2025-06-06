package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Warehouse;
import com.ecommerce.service.interfaces.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Warehouse>>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(ApiResponse.success(warehouses));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Warehouse>>> getActiveWarehouses() {
        List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success(warehouses));
    }
    
    @GetMapping("/proximity")
    public ResponseEntity<ApiResponse<List<Warehouse>>> getWarehousesByProximity(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusInKm) {
        List<Warehouse> warehouses = warehouseService.findWarehousesByProximity(latitude, longitude, radiusInKm);
        return ResponseEntity.ok(ApiResponse.success(warehouses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Warehouse>> getWarehouseById(@PathVariable Long id) {
        Optional<Warehouse> warehouseOpt = warehouseService.getWarehouseById(id);
        if (warehouseOpt.isEmpty()) {
            ApiResponse<Warehouse> errorResponse = ApiResponse.error(
                "Warehouse not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Warehouse.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(warehouseOpt.get()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Warehouse>> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        Warehouse createdWarehouse = warehouseService.createWarehouse(warehouse);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdWarehouse, "Warehouse created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Warehouse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody Warehouse warehouse) {
        
        Optional<Warehouse> warehouseOpt = warehouseService.getWarehouseById(id);
        if (warehouseOpt.isEmpty()) {
            ApiResponse<Warehouse> errorResponse = ApiResponse.error(
                "Warehouse not found with id: " + id, 
                HttpStatus.NOT_FOUND.value(), 
                Warehouse.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        warehouse.setId(id);
        Warehouse updatedWarehouse = warehouseService.updateWarehouse(id, warehouse);
        return ResponseEntity.ok(ApiResponse.success(updatedWarehouse, "Warehouse updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        Optional<Warehouse> warehouseOpt = warehouseService.getWarehouseById(id);
        if (warehouseOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Warehouse not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deleted successfully"));
    }
}
