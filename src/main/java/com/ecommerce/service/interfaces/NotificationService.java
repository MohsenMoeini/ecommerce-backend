package com.ecommerce.service.interfaces;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    /**
     * Send an order confirmation notification
     * @param userId The user to notify
     * @param orderId The order ID
     * @return true if notification was sent successfully
     */
    boolean sendOrderConfirmation(Long userId, Long orderId);
    
    /**
     * Send shipping update notification
     * @param userId The user to notify
     * @param orderId The order ID
     * @param status New shipping status
     * @return true if notification was sent successfully
     */
    boolean sendShippingUpdate(Long userId, Long orderId, String status);
    
    /**
     * Send a notification when an item on user's wishlist is back in stock
     * @param userId The user to notify
     * @param productId The product ID that's back in stock
     * @return true if notification was sent successfully
     */
    boolean sendBackInStockNotification(Long userId, Long productId);
    
    /**
     * Send a notification when an item on user's wishlist has a price drop
     * @param userId The user to notify
     * @param productId The product ID with price change
     * @param oldPrice Previous price
     * @param newPrice New lower price
     * @return true if notification was sent successfully
     */
    boolean sendPriceDropNotification(Long userId, Long productId, double oldPrice, double newPrice);
    
    /**
     * Send a custom notification with variable parameters
     * @param userId The user to notify
     * @param template Template identifier for the notification
     * @param params Map of parameters to substitute in the template
     * @return true if notification was sent successfully
     */
    boolean sendCustomNotification(Long userId, String template, Map<String, Object> params);
    
    /**
     * Get all unread notifications for a user
     * @param userId The user ID
     * @return List of notification data
     */
    List<Map<String, Object>> getUserNotifications(Long userId);
    
    /**
     * Mark notifications as read
     * @param notificationIds List of notification IDs to mark as read
     * @return number of notifications successfully marked as read
     */
    int markNotificationsAsRead(List<Long> notificationIds);
    
    /**
     * Send system alert to administrators
     * @param alertType Type of alert (LOW_INVENTORY, PAYMENT_FAILURE, etc.)
     * @param message Alert message
     * @param data Additional context data
     * @return true if alert was sent successfully
     */
    boolean sendAdminAlert(String alertType, String message, Map<String, Object> data);
}
