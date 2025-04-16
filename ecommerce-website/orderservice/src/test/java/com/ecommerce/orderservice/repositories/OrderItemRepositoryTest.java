package com.ecommerce.orderservice.repositories;

import com.ecommerce.orderservice.models.OrderStatus;
import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create and save an order with a valid status
        testOrder = Order.builder()
                .userId(101L)
                .status(OrderStatus.PENDING)
                .totalAmount(250.00)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        testOrder = orderRepository.save(testOrder); // Save in DB

        OrderItem item1 = new OrderItem(null, 10L, 2, 125.50, testOrder, false);
        OrderItem item2 = new OrderItem(null, 20L, 1, 200.75, testOrder, false);

        orderItemRepository.saveAll(List.of(item1, item2));
    }

    @Test
    void testFindByOrderId() {
        List<OrderItem> items = orderItemRepository.findByOrderId(testOrder.getId());
        assertNotNull(items);
        assertEquals(2, items.size());
    }
}
