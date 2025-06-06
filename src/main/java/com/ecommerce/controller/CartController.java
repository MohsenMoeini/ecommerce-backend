package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.User;
import com.ecommerce.service.interfaces.CartService;
import com.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItem>>> getCartItems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<List<CartItem>> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), (Class<List<CartItem>>) (Class<?>) List.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        List<CartItem> cartItems = cartService.getCartItems(userOpt.get().getId());
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItem>> addItemToCart(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<CartItem> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), CartItem.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            CartItem cartItem = cartService.addItemToCart(userOpt.get().getId(), productId, quantity);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(cartItem, "Item added to cart successfully"));
        } catch (IllegalArgumentException e) {
            ApiResponse<CartItem> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), CartItem.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/items")
    public ResponseEntity<ApiResponse<CartItem>> updateCartItem(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<CartItem> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), CartItem.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        try {
            CartItem updatedItem = cartService.updateCartItem(userOpt.get().getId(), productId, quantity);
            return ResponseEntity.ok(ApiResponse.success(updatedItem, "Cart item updated successfully"));
        } catch (IllegalArgumentException e) {
            ApiResponse<CartItem> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value(), CartItem.class);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> removeItemFromCart(@RequestParam Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
        
        boolean removed = cartService.removeItemFromCart(userOpt.get().getId(), productId);
        if (removed) {
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found in cart", HttpStatus.NOT_FOUND.value()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
        
        cartService.clearCart(userOpt.get().getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
}
