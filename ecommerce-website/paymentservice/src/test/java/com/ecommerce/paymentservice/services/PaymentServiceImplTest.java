package com.ecommerce.paymentservice.services;

import com.ecommerce.paymentservice.dtos.OrderEvent;
import com.ecommerce.paymentservice.dtos.PaymentEvent;
import com.ecommerce.paymentservice.kafka.PaymentProducer;
import com.ecommerce.paymentservice.models.Payment;
import com.ecommerce.paymentservice.models.PaymentStatus;
import com.ecommerce.paymentservice.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProducer paymentProducer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private WebClient webClient;

    @Value("${kafka.topic.payment-status}")
    private String paymentTopic;

    @Value("${orderservice.base-url}")
    private String orderServiceBaseUrl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    public void testCreatePaymentLink() {
        Long orderId = 1L;
        Long userId = 1L;
        Double amount = 100.0;

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentId(UUID.randomUUID().toString())
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        String paymentLink = paymentService.createPaymentLink(orderId, userId, amount);

        assertThat(paymentLink).isNotNull();
        assertThat(paymentLink).contains("http://localhost:8082/payments/process/");
    }

    @Test
    public void testProcessPayment_OrderCanceled() {
        OrderEvent orderEvent = new OrderEvent(1L, 1L, 100.0, new ArrayList<>(), "CANCELED", LocalDateTime.now());

        when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just("CANCELED"));

        PaymentEvent paymentEvent = paymentService.processPayment(orderEvent);

        assertThat(paymentEvent.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentProducer, never()).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    public void testProcessPayment_Success() {
        OrderEvent orderEvent = new OrderEvent(1L, 1L, 100.0, new ArrayList<>(), "NEW", LocalDateTime.now());

        when(webClient.get().uri(anyString()).retrieve().bodyToMono(String.class))
                .thenReturn(Mono.just("NEW"));

        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .paymentId(UUID.randomUUID().toString())
                .amount(orderEvent.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentEvent paymentEvent = paymentService.processPayment(orderEvent);

        assertThat(paymentEvent).isNotNull();
        assertThat(paymentEvent.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    public void testProcessPayment_UpdateFailed() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .orderId(1L)
                .userId(1L)
                .paymentId(paymentId)
                .amount(100.0)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        OrderEvent orderEvent = new OrderEvent(1L, 1L, 100.0, new ArrayList<>(), "NEW", LocalDateTime.now());

        when(webClientBuilder.build().get().uri(anyString())
                .retrieve().bodyToMono(OrderEvent.class))
                .thenReturn(Mono.just(orderEvent));

        when(webClientBuilder.build().put()
                .uri(anyString(), any(Object[].class))
                .retrieve()
                .bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        boolean status = paymentService.processPayment(paymentId, "failed");

        assertThat(status).isFalse();
        verify(paymentProducer).sendPaymentEvent(any(PaymentEvent.class));
        // Verify that the PUT chain was called once.
        verify(webClientBuilder.build().put(), times(1))
                .uri(anyString(), any(Object[].class));
    }

    @Test
    public void testProcessPayment_UpdateSuccess() {
        String paymentId = UUID.randomUUID().toString();
        Payment payment = Payment.builder()
                .orderId(1L)
                .userId(1L)
                .paymentId(paymentId)
                .amount(100.0)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        OrderEvent orderEvent = new OrderEvent(1L, 1L, 100.0, new ArrayList<>(), "NEW", LocalDateTime.now());

        when(webClient.get().uri(anyString())
                .retrieve().bodyToMono(OrderEvent.class))
                .thenReturn(Mono.just(orderEvent));

        when(webClient.put().uri(anyString(), any(Object[].class))
                .retrieve().bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        boolean status = paymentService.processPayment(paymentId, "success");

        assertThat(status).isTrue();
        verify(paymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    public void testProcessPaymentCallback() {
        String paymentId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderId(1L)
                .userId(1L)
                .paymentId(paymentId)
                .amount(100.0)
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(payment));

        OrderEvent orderEvent = new OrderEvent(1L, 1L, 100.0, new ArrayList<>(), "NEW", LocalDateTime.now());
        when(webClientBuilder.build().get().uri(anyString())
                .retrieve().bodyToMono(OrderEvent.class))
                .thenReturn(Mono.just(orderEvent));

        when(webClientBuilder.build().put().uri(anyString(), any(Object[].class))
                .retrieve().bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        paymentService.processPaymentCallback(paymentId, "success");

        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
    }

}