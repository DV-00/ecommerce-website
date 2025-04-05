package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.dtos.PaymentEvent;
import com.ecommerce.paymentservice.models.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class PaymentProducerTest {

    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @InjectMocks
    private PaymentProducer paymentProducer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(paymentProducer, "paymentTopic", "test-payment-topic");
    }

    @Test
    public void testSendPaymentEvent() {
        PaymentEvent paymentEvent = new PaymentEvent(1L, 1L, "123", PaymentStatus.PENDING, LocalDateTime.now());

        paymentProducer.sendPaymentEvent(paymentEvent);

        verify(kafkaTemplate).send(eq("test-payment-topic"), eq(paymentEvent));
    }
}