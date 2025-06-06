package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByOrderedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByOrderStatus(OrderStatus orderStatus);
}
