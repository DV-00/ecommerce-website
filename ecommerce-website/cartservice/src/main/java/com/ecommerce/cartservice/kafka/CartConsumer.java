package com.ecommerce.cartservice.kafka;

import com.ecommerce.cartservice.dtos.CartClearEvent;
import com.ecommerce.cartservice.services.CartService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartConsumer {
    private static final Logger logger = LoggerFactory.getLogger(CartConsumer.class);
    private final CartService cartService;

    @KafkaListener(topics = "cart-events", groupId = "cart-group")
    public void handleCartClearEvent(CartClearEvent cartClearEvent) {

        logger.info("🛒 Received CartClearEvent for User ID: {}", cartClearEvent.getUserId());

        if (cartClearEvent.getUserId() != null) {
            cartService.clearCart(cartClearEvent.getUserId());
            logger.info("✅ Cart cleared for User ID: {}", cartClearEvent.getUserId());
        } else {
            logger.warn("⚠️ Received CartClearEvent with NULL userId");
        }
    }
}
