package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartService {
    Cart getOrCreateCart(Long userId);
    Optional<Cart> getCartByUserId(Long userId);
    CartItem addItemToCart(Long userId, Long productId, int quantity);
    CartItem updateCartItem(Long userId, Long productId, int quantity);
    boolean removeItemFromCart(Long userId, Long productId);
    void clearCart(Long userId);
    List<CartItem> getCartItems(Long userId);
}
