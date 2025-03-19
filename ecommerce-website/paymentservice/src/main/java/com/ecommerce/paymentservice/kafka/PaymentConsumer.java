package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.dtos.OrderEvent;
import com.ecommerce.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private static final Logger logger = LoggerFactory.getLogger(PaymentConsumer.class);
    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void processOrder(OrderEvent orderEvent) {
        logger.info("Received Order Event: {}", orderEvent);

        if (orderEvent.getItems() == null) {
            logger.warn("OrderEvent items is null. Setting items to an empty list.");
            orderEvent.setItems(new ArrayList<>());
        }

        logger.info("Payment processing will be handled manually via the payment link.");
    }
}
