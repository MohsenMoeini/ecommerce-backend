package com.ecommerce.service.impl;

import com.ecommerce.entity.Inventory;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.interfaces.InventoryService;
import com.ecommerce.service.interfaces.OrderService;
import com.ecommerce.service.interfaces.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // Set order date if not already set
        if (order.getOrderedAt() == null) {
            order.setOrderedAt(LocalDateTime.now());
        }
        
        // Set initial status if not set
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(Order.OrderStatus.PROCESSING);
        }
        
        // Set initial payment status if not set
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
        }
        
        // Calculate total amount and set product references
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            
            for (OrderItem item : order.getOrderItems()) {
                // Set order reference
                item.setOrder(order);
                
                Optional<Product> productOpt = productService.getProductById(item.getProduct().getId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    // Use product price from database
                    item.setUnitPrice(product.getPrice());
                    BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    item.setSubtotal(itemTotal);
                    total = total.add(itemTotal);
                    
                    // Check and reserve inventory
                    List<Inventory> inventoryItems = inventoryService.getInventoryByProductId(product.getId());
                    if (inventoryItems.isEmpty()) {
                        throw new RuntimeException("No inventory found for product ID: " + product.getId());
                    }
                    
                    // Try to reserve from the first available inventory item
                    // In a real implementation, this would be more sophisticated
                    Inventory inventory = inventoryItems.get(0);
                    if (inventory.getQuantity() - inventory.getReservedQuantity() < item.getQuantity()) {
                        throw new RuntimeException("Insufficient inventory for product ID: " + product.getId());
                    }
                    
                    inventoryService.reserveInventory(inventory.getId(), item.getQuantity());
                } else {
                    throw new EntityNotFoundException("Product not found with id: " + item.getProduct().getId());
                }
            }
            order.setTotalAmount(total);
        }
        
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderedAtBetween(startDate, endDate);
    }

    @Override
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        Order.OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        
        // Handle inventory adjustments based on status changes
        if (oldStatus == Order.OrderStatus.CANCELLED && newStatus != Order.OrderStatus.CANCELLED) {
            // Re-reserve inventory if order is no longer cancelled
            for (OrderItem item : order.getOrderItems()) {
                List<Inventory> inventoryItems = inventoryService.getInventoryByProductId(item.getProduct().getId());
                if (inventoryItems.isEmpty()) {
                    throw new RuntimeException("No inventory found for product ID: " + item.getProduct().getId());
                }
                
                // Try to reserve from the first available inventory
                Inventory inventory = inventoryItems.get(0);
                if (inventory.getQuantity() - inventory.getReservedQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Cannot update status: insufficient inventory for product ID: " + item.getProduct().getId());
                }
                
                inventoryService.reserveInventory(inventory.getId(), item.getQuantity());
            }
        } else if (oldStatus != Order.OrderStatus.CANCELLED && newStatus == Order.OrderStatus.CANCELLED) {
            // Release inventory if order is now cancelled
            releaseInventoryForOrder(order);
        } else if (oldStatus == Order.OrderStatus.PROCESSING && newStatus == Order.OrderStatus.SHIPPED) {
            // When shipping, we actually deduct from inventory (reserved -> consumed)
            confirmInventoryForOrder(order);
        }
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Can only cancel if not shipped or delivered
        if (order.getOrderStatus() == Order.OrderStatus.SHIPPED || 
            order.getOrderStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel order that has been shipped or delivered");
        }
        
        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        
        // Release inventory
        releaseInventoryForOrder(order);
        
        orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // If order is not cancelled, release inventory
        if (order.getOrderStatus() != Order.OrderStatus.CANCELLED) {
            releaseInventoryForOrder(order);
        }
        
        orderRepository.delete(order);
    }
    
    @Override
    @Transactional
    public Order updatePaymentStatus(Long orderId, Order.PaymentStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        order.setPaymentStatus(newStatus);
        return orderRepository.save(order);
    }
    
    /**
     * Helper method to release inventory for all items in an order
     */
    private void releaseInventoryForOrder(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                List<Inventory> inventoryItems = inventoryService.getInventoryByProductId(item.getProduct().getId());
                if (!inventoryItems.isEmpty()) {
                    // In a real implementation, we would need to track which inventory item was reserved
                    Inventory inventory = inventoryItems.get(0);
                    inventoryService.releaseReservedInventory(inventory.getId(), item.getQuantity());
                }
            }
        }
    }
    
    /**
     * Helper method to confirm inventory deduction for all items in an order
     */
    private void confirmInventoryForOrder(Order order) {
        // In a real implementation, this would move items from "reserved" to "sold"
        // For now, we'll just ensure the inventory is still reserved
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                List<Inventory> inventoryItems = inventoryService.getInventoryByProductId(item.getProduct().getId());
                if (inventoryItems.isEmpty()) {
                    throw new RuntimeException("No inventory found for product ID: " + item.getProduct().getId());
                }
            }
        }
    }
}
