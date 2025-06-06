package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(Order order);
    Optional<Order> getOrderById(Long orderId);
    List<Order> getOrdersByUserId(Long userId);
    Page<Order> getOrdersByUserId(Long userId, Pageable pageable);
    List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> getOrdersByStatus(Order.OrderStatus status);
    Order updateOrderStatus(Long orderId, Order.OrderStatus status);
    Order updatePaymentStatus(Long orderId, Order.PaymentStatus status);
    void cancelOrder(Long orderId);
    void deleteOrder(Long orderId);
}
