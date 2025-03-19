package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.dtos.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProducer.class);
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${kafka.topic.payment-status}")
    private String paymentTopic;

    public void sendPaymentEvent(PaymentEvent paymentEvent) {
        logger.info("🚀 Sending PaymentEvent to topic '{}': {}", paymentTopic, paymentEvent);
        kafkaTemplate.send(paymentTopic, paymentEvent);
    }
}
