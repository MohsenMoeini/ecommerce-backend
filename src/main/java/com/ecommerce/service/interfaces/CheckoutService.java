package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Order;

public interface CheckoutService {
    /**
     * Process a complete checkout from the user's cart
     * @param userId The ID of the user checking out
     * @param addressId The shipping address ID
     * @param paymentMethod The payment method identifier
     * @param paymentDetails Additional payment details if needed
     * @return The created order
     */
    Order checkout(Long userId, Long addressId, String paymentMethod, String paymentDetails);
    
    /**
     * Validate if all items in the user's cart are available for purchase
     * @param userId The ID of the user
     * @return True if the cart is valid for checkout, false otherwise
     */
    boolean validateCart(Long userId);
    
    /**
     * Calculate shipping costs based on address and items
     * @param userId The ID of the user
     * @param addressId The shipping address ID
     * @return The calculated shipping cost
     */
    double calculateShippingCost(Long userId, Long addressId);
    
    /**
     * Apply a coupon code to the user's cart
     * @param userId The ID of the user
     * @param couponCode The coupon code to apply
     * @return The discount amount applied
     */
    double applyCoupon(Long userId, String couponCode);
}
