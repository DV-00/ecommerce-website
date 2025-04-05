package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.dtos.OrderEvent;
import com.ecommerce.productservice.dtos.OrderItemDto;
import com.ecommerce.productservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.Mockito.*;

class OrderEventConsumerTest {

    @Mock
    private ProductService productService;

    private OrderEventConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new OrderEventConsumer(productService);
    }

    @Test
    void testConsumeOrderEventWithValidCanceledOrder() {
        OrderItemDto item1 = new OrderItemDto();
        item1.setProductId(1L);
        item1.setQuantity(5);

        OrderItemDto item2 = new OrderItemDto();
        item2.setProductId(2L);
        item2.setQuantity(3);

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("CANCELED");
        orderEvent.setItems(Arrays.asList(item1, item2));

        consumer.consumeOrderEvent(orderEvent);

        // Verify that restoreStock is called for each item
        verify(productService, times(1)).restoreStock(1L, 5);
        verify(productService, times(1)).restoreStock(2L, 3);
    }

    @Test
    void testConsumeOrderEventWithInvalidEvent() {
        // Test with a null order event
        consumer.consumeOrderEvent(null);

        OrderEvent emptyEvent = new OrderEvent();
        emptyEvent.setStatus("CANCELED");
        emptyEvent.setItems(Collections.emptyList());
        consumer.consumeOrderEvent(emptyEvent);

        verifyNoInteractions(productService);
    }

    @Test
    void testConsumeOrderEventWithNonCanceledStatus() {
        OrderItemDto item = new OrderItemDto();
        item.setProductId(1L);
        item.setQuantity(2);

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("COMPLETED");
        orderEvent.setItems(Arrays.asList(item));

        consumer.consumeOrderEvent(orderEvent);

        verifyNoInteractions(productService);
    }
}
