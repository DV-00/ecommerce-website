package com.ecommerce.paymentservice.services;

import com.ecommerce.paymentservice.dtos.OrderEvent;
import com.ecommerce.paymentservice.dtos.PaymentEvent;
import com.ecommerce.paymentservice.kafka.PaymentProducer;
import com.ecommerce.paymentservice.models.Payment;
import com.ecommerce.paymentservice.models.PaymentStatus;
import com.ecommerce.paymentservice.repositories.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final String paymentTopic;
    private final WebClient.Builder webClientBuilder;
    private final String orderServiceBaseUrl;

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentProducer paymentProducer,
            @Value("${kafka.topic.payment-status}") String paymentTopic,
            WebClient.Builder webClientBuilder,
            @Value("${orderservice.base-url}") String orderServiceBaseUrl) {
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
        this.paymentTopic = paymentTopic;
        this.webClientBuilder = webClientBuilder;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    // Create operation
    @Override
    public String createPaymentLink(Long orderId, Long userId, Double amount) {
        try {
            String paymentId = UUID.randomUUID().toString();
            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .paymentId(paymentId)
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);
            return "http://localhost:8082/payments/process/" + paymentId;
        } catch (Exception ex) {
            logger.error("Error creating payment link for orderId: {}, userId: {}. Error: {}", orderId, userId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to create payment link", ex);
        }
    }

    // Update operation
    @Override
    public PaymentEvent processPayment(OrderEvent orderEvent) {
        try {
            if (orderEvent.getItems() == null) {
                logger.warn("OrderEvent.getItems() is null for order ID: {}. Initializing to empty list.", orderEvent.getOrderId());
                orderEvent.setItems(new ArrayList<>());
            }
            String orderStatus = webClientBuilder.build()
                    .get()
                    .uri(orderServiceBaseUrl + "/api/orders/" + orderEvent.getOrderId() + "/status")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if ("CANCELED".equalsIgnoreCase(orderStatus)) {
                logger.warn("Order ID {} is already canceled. Skipping payment.", orderEvent.getOrderId());
                return new PaymentEvent(orderEvent.getOrderId(), orderEvent.getUserId(), null, PaymentStatus.FAILED, LocalDateTime.now());
            }
            String paymentId = UUID.randomUUID().toString();
            PaymentStatus status = PaymentStatus.PENDING;
            Payment payment = Payment.builder()
                    .orderId(orderEvent.getOrderId())
                    .userId(orderEvent.getUserId())
                    .paymentId(paymentId)
                    .amount(orderEvent.getTotalAmount())
                    .status(status)
                    .build();
            paymentRepository.save(payment);
            PaymentEvent paymentEvent = new PaymentEvent(
                    orderEvent.getOrderId(),
                    orderEvent.getUserId(),
                    paymentId,
                    status,
                    LocalDateTime.now()
            );
            paymentProducer.sendPaymentEvent(paymentEvent);
            logger.info("Sent PaymentEvent: {}", paymentEvent);
            if (status == PaymentStatus.SUCCESS) {
                logger.info("Payment successful for order ID: {}. Stock was already reserved during order placement.", orderEvent.getOrderId());
            } else {
                webClientBuilder.build()
                        .patch()
                        .uri(orderServiceBaseUrl + "/api/orders/cancel/" + orderEvent.getOrderId())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
                logger.warn("Order ID {} has been canceled due to payment failure.", orderEvent.getOrderId());
            }
            return paymentEvent;
        } catch (Exception ex) {
            logger.error("Error processing payment for OrderEvent {}: {}", orderEvent, ex.getMessage(), ex);
            throw new RuntimeException("Error processing payment", ex);
        }
    }

    // Update operation
    @Override
    public boolean processPayment(String paymentId, String status) {
        try {
            Payment payment = paymentRepository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new RuntimeException("Invalid Payment ID!"));
            PaymentStatus paymentStatus = status.equalsIgnoreCase("success") ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            payment.setStatus(paymentStatus);
            paymentRepository.save(payment);
            OrderEvent orderEvent = webClientBuilder.build()
                    .get()
                    .uri(orderServiceBaseUrl + "/api/orders/event/" + payment.getOrderId())
                    .retrieve()
                    .bodyToMono(OrderEvent.class)
                    .block();
            if (orderEvent.getItems() == null) {
                logger.warn("OrderEvent.getItems() is null for order ID: {}. Initializing to empty list.", orderEvent.getOrderId());
                orderEvent.setItems(new ArrayList<>());
            }
            PaymentEvent paymentEvent = new PaymentEvent(
                    payment.getOrderId(),
                    orderEvent.getUserId(),
                    payment.getPaymentId(),
                    paymentStatus,
                    LocalDateTime.now()
            );
            paymentProducer.sendPaymentEvent(paymentEvent);
            logger.info("Payment status updated: {}", paymentEvent);
            if (paymentStatus == PaymentStatus.SUCCESS) {
                logger.info("Payment successful for order ID: {}. Stock was already reserved.", payment.getOrderId());
            } else {
                webClientBuilder.build()
                        .put()
                        .uri(orderServiceBaseUrl + "/api/orders/cancel/" + payment.getOrderId())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
                logger.warn("Order ID {} has been canceled due to payment failure.", payment.getOrderId());
            }
            return paymentStatus == PaymentStatus.SUCCESS;
        } catch (Exception ex) {
            logger.error("Error processing payment for Payment ID {}: {}", paymentId, ex.getMessage(), ex);
            throw new RuntimeException("Error processing payment", ex);
        }
    }

    // Callback operation
    @Override
    public void processPaymentCallback(String paymentId, String status) {
        processPayment(paymentId, status);
    }
}
