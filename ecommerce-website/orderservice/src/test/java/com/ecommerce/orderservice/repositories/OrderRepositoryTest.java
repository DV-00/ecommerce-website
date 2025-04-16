package com.ecommerce.orderservice.repositories;

import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order order1, order2, expiredOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        order1 = Order.builder()
                .userId(101L)
                .status(OrderStatus.PENDING)
                .totalAmount(250.00)
                .createdAt(LocalDateTime.now().minusMinutes(5)) // Recent order
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // Still valid
                .build();

        order2 = Order.builder()
                .userId(102L)
                .status(OrderStatus.COMPLETED)
                .totalAmount(150.00)
                .createdAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().minusDays(1).plusMinutes(10))
                .build();

        // Expired order
        expiredOrder = Order.builder()
                .userId(103L)
                .status(OrderStatus.PENDING)
                .totalAmount(300.00)
                .createdAt(LocalDateTime.now().minusMinutes(20)) // Older order
                .expiresAt(LocalDateTime.now().minusMinutes(10)) // Already expired
                .build();

        orderRepository.saveAll(List.of(order1, order2, expiredOrder));
        orderRepository.flush();
    }

    @Test
    void testFindByUserId() {
        List<Order> orders = orderRepository.findByUserId(101L);
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.PENDING, orders.get(0).getStatus());
    }

    @Test
    void testFindByStatus() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        assertNotNull(pendingOrders);
        assertEquals(2, pendingOrders.size());

        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);
        assertNotNull(completedOrders);
        assertEquals(1, completedOrders.size());
    }
}
