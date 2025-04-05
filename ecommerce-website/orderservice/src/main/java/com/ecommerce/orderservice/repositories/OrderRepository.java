package com.ecommerce.orderservice.repositories;

import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime timestamp);

}
