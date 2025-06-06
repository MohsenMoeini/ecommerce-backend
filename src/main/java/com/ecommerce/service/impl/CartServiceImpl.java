package com.ecommerce.service.impl;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.interfaces.CartService;
import com.ecommerce.service.interfaces.ProductService;
import com.ecommerce.service.interfaces.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;

    @Override
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        // Get user object
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
                
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartItems(new HashSet<>());
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public CartItem addItemToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        // Get product to verify it exists and get its current price
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        
        // Get or create cart
        Cart cart = getOrCreateCart(userId);
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        
        if (existingItem.isPresent()) {
            // Update existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setUnitPrice(product.getPrice()); // Update to current price
            return cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            return cartItemRepository.save(newItem);
        }
    }

    @Override
    @Transactional
    public CartItem updateCartItem(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(userId, productId) ? null : null;
        }
        
        // Get cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user"));
        
        // Find cart item
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found in cart"));
        
        // Update quantity
        item.setQuantity(quantity);
        
        // Update price to current product price
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        item.setUnitPrice(product.getPrice());
        
        return cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public boolean removeItemFromCart(Long userId, Long productId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return false;
        }
        
        Cart cart = cartOpt.get();
        Optional<CartItem> itemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (itemOpt.isEmpty()) {
            return false;
        }
        
        CartItem item = itemOpt.get();
        cartItemRepository.delete(item);
        return true;
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            // Delete all cart items for this cart
            cartItemRepository.deleteByCartId(cart.getId());
            
            // Reload cart to update its state
            cart.setCartItems(new HashSet<>());
            cartRepository.save(cart);
        }
    }

    @Override
    public List<CartItem> getCartItems(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        Cart cart = cartOpt.get();
        return cartItemRepository.findByCartId(cart.getId());
    }
}
