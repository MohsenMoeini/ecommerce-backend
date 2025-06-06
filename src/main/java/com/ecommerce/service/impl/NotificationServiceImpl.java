package com.ecommerce.service.impl;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.service.interfaces.NotificationService;
import com.ecommerce.service.interfaces.OrderService;
import com.ecommerce.service.interfaces.ProductService;
import com.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    
    // In-memory store of notifications for demo purposes
    // In a real application, this would be stored in a database
    private final Map<Long, List<Map<String, Object>>> userNotifications = new ConcurrentHashMap<>();
    private long nextNotificationId = 1;

    @Override
    public boolean sendOrderConfirmation(Long userId, Long orderId) {
        logger.info("Sending order confirmation notification to user {} for order {}", userId, orderId);
        
        // Get order details
        Order order = orderService.getOrderById(orderId)
                .orElse(null);
        
        if (order == null) {
            logger.error("Failed to send order confirmation: Order {} not found", orderId);
            return false;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", orderId);
        params.put("orderDate", order.getOrderedAt());
        params.put("totalAmount", order.getTotalAmount());
        
        return sendCustomNotification(userId, "ORDER_CONFIRMATION", params);
    }

    @Override
    public boolean sendShippingUpdate(Long userId, Long orderId, String status) {
        logger.info("Sending shipping update notification to user {} for order {} with status {}",
                userId, orderId, status);
                
        Map<String, Object> params = new HashMap<>();
        params.put("orderNumber", orderId);
        params.put("shippingStatus", status);
        params.put("updateTime", LocalDateTime.now());
        
        return sendCustomNotification(userId, "SHIPPING_UPDATE", params);
    }

    @Override
    public boolean sendBackInStockNotification(Long userId, Long productId) {
        logger.info("Sending back-in-stock notification to user {} for product {}", userId, productId);
        
        Product product = productService.getProductById(productId)
                .orElse(null);
                
        if (product == null) {
            logger.error("Failed to send back-in-stock notification: Product {} not found", productId);
            return false;
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("productName", product.getName());
        params.put("restockTime", LocalDateTime.now());
        
        return sendCustomNotification(userId, "BACK_IN_STOCK", params);
    }

    @Override
    public boolean sendPriceDropNotification(Long userId, Long productId, double oldPrice, double newPrice) {
        logger.info("Sending price drop notification to user {} for product {} (price changed from {} to {})",
                userId, productId, oldPrice, newPrice);
                
        Product product = productService.getProductById(productId)
                .orElse(null);
                
        if (product == null) {
            logger.error("Failed to send price drop notification: Product {} not found", productId);
            return false;
        }
        
        double discountPercent = (1 - (newPrice / oldPrice)) * 100;
        
        Map<String, Object> params = new HashMap<>();
        params.put("productId", productId);
        params.put("productName", product.getName());
        params.put("oldPrice", oldPrice);
        params.put("newPrice", newPrice);
        params.put("discountPercent", String.format("%.1f", discountPercent));
        
        return sendCustomNotification(userId, "PRICE_DROP", params);
    }

    @Override
    public boolean sendCustomNotification(Long userId, String template, Map<String, Object> params) {
        logger.info("Sending custom notification of type {} to user {}", template, userId);
        
        // Create notification object
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", nextNotificationId++);
        notification.put("userId", userId);
        notification.put("type", template);
        notification.put("params", params);
        notification.put("timestamp", LocalDateTime.now());
        notification.put("read", false);
        
        // Store notification
        userNotifications
            .computeIfAbsent(userId, k -> new ArrayList<>())
            .add(notification);
            
        // In a real application, this would send the notification through various channels
        // such as email, SMS, push notification, etc.
        // For this implementation, we'll just log it
        logger.info("Notification sent: {}", notification);
        
        return true;
    }

    @Override
    public List<Map<String, Object>> getUserNotifications(Long userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public int markNotificationsAsRead(List<Long> notificationIds) {
        int count = 0;
        
        // Iterate through all users' notifications
        for (List<Map<String, Object>> notifications : userNotifications.values()) {
            for (Map<String, Object> notification : notifications) {
                Long notificationId = (Long) notification.get("id");
                
                if (notificationIds.contains(notificationId) && !(boolean) notification.get("read")) {
                    notification.put("read", true);
                    count++;
                }
            }
        }
        
        return count;
    }

    @Override
    public boolean sendAdminAlert(String alertType, String message, Map<String, Object> data) {
        logger.warn("ADMIN ALERT - {}: {} - Data: {}", alertType, message, data);
        
        // In a real application, this would send alerts to administrators through
        // various channels such as email, SMS, dashboard notifications, etc.
        // It might also create tickets in an internal system.
        
        return true;
    }
}
