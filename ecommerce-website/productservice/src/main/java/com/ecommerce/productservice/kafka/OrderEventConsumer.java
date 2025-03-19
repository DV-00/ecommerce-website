package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.dtos.OrderEvent;
import com.ecommerce.productservice.dtos.OrderItemDto;
import com.ecommerce.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final ProductService productService;

    @KafkaListener(topics = "order-events", groupId = "product-service-group")
    public void consumeOrderEvent(OrderEvent orderEvent) {
        logger.info("Received OrderEvent: {}", orderEvent);

        if (orderEvent == null || orderEvent.getItems() == null || orderEvent.getItems().isEmpty()) {
            logger.warn("Invalid OrderEvent received: {}", orderEvent);
            return;
        }

        // If order is canceled, restore stock for each order item
        if ("CANCELED".equalsIgnoreCase(orderEvent.getStatus())) {
            for (OrderItemDto item : orderEvent.getItems()) {
                try {
                    productService.restoreStock(item.getProductId(), item.getQuantity());
                    logger.info("Stock restored for Product ID: {} | Quantity: {}",
                            item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    logger.error("Error restoring stock for Product ID: {} | Quantity: {} | Error: {}",
                            item.getProductId(), item.getQuantity(), e.getMessage());
                }
            }
        }
    }
}
