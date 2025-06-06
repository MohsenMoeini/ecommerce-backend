package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.service.interfaces.CheckoutService;
import com.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> processCheckout(
            @RequestParam Long addressId,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String paymentDetails) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Order> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), Order.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            Order order = checkoutService.checkout(
                userOpt.get().getId(), 
                addressId, 
                paymentMethod, 
                paymentDetails != null ? paymentDetails : "");
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Checkout successful"));
        } catch (IllegalStateException e) {
            ApiResponse<Order> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Order.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/validate-cart")
    public ResponseEntity<ApiResponse<Boolean>> validateCart() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Boolean> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), Boolean.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        boolean isValid = checkoutService.validateCart(userOpt.get().getId());
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }

    @GetMapping("/shipping-cost")
    public ResponseEntity<ApiResponse<Double>> calculateShippingCost(@RequestParam Long addressId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Double> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), Double.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            double shippingCost = checkoutService.calculateShippingCost(userOpt.get().getId(), addressId);
            return ResponseEntity.ok(ApiResponse.success(shippingCost));
        } catch (IllegalArgumentException e) {
            ApiResponse<Double> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Double.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/apply-coupon")
    public ResponseEntity<ApiResponse<Double>> applyCoupon(@RequestParam String couponCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Double> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), Double.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            double discountAmount = checkoutService.applyCoupon(userOpt.get().getId(), couponCode);
            return ResponseEntity.ok(ApiResponse.success(discountAmount, "Coupon applied successfully"));
        } catch (IllegalArgumentException e) {
            ApiResponse<Double> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Double.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
