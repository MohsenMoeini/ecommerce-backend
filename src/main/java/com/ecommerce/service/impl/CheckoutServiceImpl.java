package com.ecommerce.service.impl;

import com.ecommerce.entity.*;
import com.ecommerce.service.interfaces.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final UserService userService;

    // In a real application, you'd have these services as well
    // private final PaymentService paymentService;
    // private final CouponService couponService;
    // private final ShippingService shippingService;

    @Override
    @Transactional
    public Order checkout(Long userId, Long addressId, String paymentMethod, String paymentDetails) {
        // Validate cart
        if (!validateCart(userId)) {
            throw new RuntimeException("Cart validation failed. Some items may be out of stock.");
        }
        
        // Get user's cart and items
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot checkout with an empty cart");
        }
        
        // Verify shipping address exists and belongs to the user
        Optional<Address> addressOpt = addressService.getAddressById(addressId);
        if (addressOpt.isEmpty() || !addressOpt.get().getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Invalid shipping address");
        }
        
        // Calculate totals
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getUnitPrice().doubleValue() * item.getQuantity();
        }
        
        // Calculate shipping
        double shippingCost = calculateShippingCost(userId, addressId);
        
        // Create new order
        Order order = new Order();
        
        // Set user object instead of just ID
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        order.setUser(user);
        
        order.setOrderedAt(LocalDateTime.now());
        order.setOrderStatus(Order.OrderStatus.PROCESSING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        
        // Set shipping address as entity object
        order.setShippingAddress(addressOpt.get());
        order.setBillingAddress(addressOpt.get()); // Using same address for billing by default
        
        // Set total amount as BigDecimal
        BigDecimal totalAmountDecimal = BigDecimal.valueOf(subtotal + shippingCost);
        order.setTotalAmount(totalAmountDecimal);
        
        order.setPaymentMethod(paymentMethod);
        order.setOrderNumber(generateOrderNumber()); // Helper method to generate unique order number
        
        // Create order items from cart items
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            
            // Set product object instead of just ID
            Product product = productService.getProductById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            orderItem.setProduct(product);
            
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            
            // Calculate subtotal for the item
            BigDecimal subtotalDecimal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setSubtotal(subtotalDecimal);
            
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        
        // In a real application: Process payment here
        // boolean paymentSuccess = paymentService.processPayment(userId, order.getTotalAmount(), paymentMethod, paymentDetails);
        // if (!paymentSuccess) {
        //     throw new RuntimeException("Payment processing failed");
        // }
        
        // Create order
        Order createdOrder = orderService.createOrder(order);
        
        // Clear the cart after successful checkout
        cartService.clearCart(userId);
        
        return createdOrder;
    }

    @Override
    public boolean validateCart(Long userId) {
        List<CartItem> cartItems = cartService.getCartItems(userId);
        return validateCartItems(cartItems);
    }

    private boolean validateCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            // Validate product exists
            Optional<Product> productOpt = productService.getProductById(item.getProduct().getId());
            if (productOpt.isEmpty()) {
                return false;
            }
            
            // Validate sufficient inventory
            List<Inventory> inventoryItems = inventoryService.getInventoryByProductId(item.getProduct().getId());
            if (inventoryItems.isEmpty()) {
                return false;
            }
            
            // Check if there's enough inventory across all warehouses
            int totalAvailable = inventoryItems.stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
                
            if (totalAvailable < item.getQuantity()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public double calculateShippingCost(Long userId, Long addressId) {
        // In a real application, this would have complex logic based on:
        // - Weight and dimensions of products
        // - Shipping distance
        // - Shipping method selected
        // - Special shipping rules or promotions
        
        // For this implementation, we'll use a simple calculation
        List<CartItem> cartItems = cartService.getCartItems(userId);
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        
        // Basic shipping cost + per item fee
        return 5.0 + (totalItems * 0.5);
    }

    @Override
    public double applyCoupon(Long userId, String couponCode) {
        // In a real application, you would:
        // 1. Validate the coupon code exists and is active
        // 2. Check if the coupon is applicable to the user's cart
        // 3. Apply the discount according to the coupon rules
        // 4. Return the discount amount
        
        // For this implementation, we'll just simulate a fixed discount
        if ("DISCOUNT20".equalsIgnoreCase(couponCode)) {
            return 20.0;
        } else if ("FREESHIP".equalsIgnoreCase(couponCode)) {
            return calculateShippingCost(userId, null); // Refund the shipping cost
        } else {
            throw new RuntimeException("Invalid coupon code");
        }
    }

    private String generateOrderNumber() {
        // Helper method to generate unique order number
        // For this implementation, we'll just use a simple incrementing number
        return "ORDER-" + System.currentTimeMillis();
    }
}
