package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal discountPrice;

    // stockQuantity will be computed from Inventory entries
    @Transient
    private Integer totalStockQuantity;

    @Column(name = "sales_count")
    private Integer salesCount = 0;

    @Column(nullable = false)
    private String sku;

    private String imageUrl;
    
    private String brand;
    
    private String manufacturer;
    
    private Double weight;
    
    private String weightUnit;
    
    private Double width;
    
    private Double height;
    
    private Double depth;
    
    private String dimensionUnit;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<Review> reviews = new HashSet<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<CartItem> cartItems = new HashSet<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<OrderItem> orderItems = new HashSet<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<Inventory> inventoryItems = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Calculate total stock quantity across all warehouses
    public Integer getTotalStockQuantity() {
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            return 0;
        }
        return inventoryItems.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
    }
    
    // Calculate available stock (not reserved) across all warehouses
    public Integer getAvailableStockQuantity() {
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            return 0;
        }
        return inventoryItems.stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
    }
    
    // Check if product is in stock
    public boolean isInStock() {
        return getAvailableStockQuantity() > 0;
    }
}
