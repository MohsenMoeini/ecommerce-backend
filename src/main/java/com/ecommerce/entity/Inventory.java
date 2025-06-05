package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;
    
    @Column(name = "reorder_threshold")
    private Integer reorderThreshold;
    
    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    private String sku;
    
    private String batchNumber;
    
    private LocalDateTime expiryDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
    
    public boolean isLowStock() {
        return reorderThreshold != null && quantity <= reorderThreshold;
    }

    public enum InventoryStatus {
        AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED
    }
}
