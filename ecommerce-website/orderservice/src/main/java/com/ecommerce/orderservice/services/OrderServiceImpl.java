package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.dtos.*;
import com.ecommerce.orderservice.exceptions.OrderNotFoundException;
import com.ecommerce.orderservice.exceptions.UserNotAuthenticatedException;
import com.ecommerce.orderservice.kafka.OrderProducer;
import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderItem;
import com.ecommerce.orderservice.models.OrderStatus;
import com.ecommerce.orderservice.repositories.OrderItemRepository;
import com.ecommerce.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final OrderItemRepository orderItemRepository;
    private final OrderProducer orderProducer;

    @Value("${userservice.base-url}")
    private String userServiceBaseUrl;

    @Value("${cartservice.base-url:http://localhost:8084}")
    private String cartServiceBaseUrl;

    @Value("${paymentservice.base-url}")
    private String paymentServiceBaseUrl;

    @Value("${productservice.base-url}")
    private String productServiceBaseUrl;

    // Create operations
    @Override
    public OrderResponse placeOrder(String token, Long userId) {
        try {
            // Authenticate user
            UserResponseDto userDto = webClientBuilder.build()
                    .get()
                    .uri(userServiceBaseUrl + "/users/validate?token=" + token)
                    .retrieve()
                    .bodyToMono(UserResponseDto.class)
                    .block();
            if (userDto == null || userDto.getUserId() == null) {
                throw new UserNotAuthenticatedException("User authentication failed!");
            }

            // Fetch cart items
            List<OrderItem> cartItems = webClientBuilder.build()
                    .get()
                    .uri(cartServiceBaseUrl + "/api/cart?userId=" + userId)
                    .retrieve()
                    .bodyToFlux(OrderItem.class)
                    .collectList()
                    .block();
            if (cartItems == null || cartItems.isEmpty()) {
                throw new RuntimeException("Cart is empty!");
            }

            // Check stock for each cart item
            for (OrderItem item : cartItems) {
                boolean isStockAvailable = checkProductStock(item.getProductId(), item.getQuantity());
                if (!isStockAvailable) {
                    throw new RuntimeException("Product " + item.getProductId() + " is out of stock!");
                }
            }

            // Get total amount of cart
            Double totalAmount = webClientBuilder.build()
                    .get()
                    .uri(cartServiceBaseUrl + "/api/cart/total?userId=" + userId)
                    .retrieve()
                    .bodyToMono(Double.class)
                    .block();
            if (totalAmount == null || totalAmount <= 0) {
                throw new RuntimeException("Failed to fetch total amount!");
            }

            // Create Order
            Order order = Order.builder()
                    .userId(userId)
                    .status(OrderStatus.PENDING)
                    .totalAmount(totalAmount)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();
            order = orderRepository.save(order);

            // Save Order Items
            for (OrderItem item : cartItems) {
                item.setOrder(order);
            }
            orderItemRepository.saveAll(cartItems);

            // After saving order items, reduce stock (reserve stock)
            try {
                for (OrderItem item : cartItems) {
                    webClientBuilder.build()
                            .patch()
                            .uri(productServiceBaseUrl + "/products/{id}/reduce-stock/{quantity}",
                                    item.getProductId(), item.getQuantity())
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();
                }
                log.info("Stock reserved (reduced) for order ID: {}", order.getId());
            } catch (Exception ex) {
                log.error("Error reducing stock for order ID {}: {}", order.getId(), ex.getMessage(), ex);
                throw new RuntimeException("Failed to reserve stock", ex);
            }

            // Request Payment Link
            String paymentLink;
            try {
                paymentLink = webClientBuilder.build()
                        .post()
                        .uri(paymentServiceBaseUrl + "/payments/create-payment-link")
                        .bodyValue(new CreatePaymentLinkRequestDto(order.getId(), totalAmount, order.getUserId()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception ex) {
                log.error("Failed to create payment link for order ID {}: {}", order.getId(), ex.getMessage(), ex);
                throw new RuntimeException("Payment service is not available.", ex);
            }

            // Publish Order Event to Kafka
            List<OrderItemDto> orderItemDtos = cartItems.stream()
                    .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList());
            OrderEvent orderEvent = new OrderEvent(
                    order.getId(),
                    userId,
                    totalAmount,
                    orderItemDtos,
                    OrderStatus.PENDING.name(),
                    order.getExpiresAt()
            );
            orderProducer.sendOrderEvent(orderEvent);

            // Return response
            return new OrderResponse(order.getId(), OrderStatus.PENDING.name(), BigDecimal.valueOf(totalAmount), paymentLink);
        } catch (Exception ex) {
            log.error("Error in placeOrder for userId {}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to place order", ex);
        }
    }

    // Scheduled operations
    @Scheduled(fixedRate = 60000) // Runs every 1 minute
    @Transactional
    public void cancelExpiredOrders() {
        try {
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, tenMinutesAgo);
            for (Order order : expiredOrders) {
                if (order.getStatus() == OrderStatus.CANCELED) {
                    log.warn("Order ID {} is already canceled. Skipping...", order.getId());
                    continue;
                }
                order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);

                List<OrderItemDto> orderItemDtos = (order.getItems() != null)
                        ? order.getItems().stream()
                        .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
                        : Collections.emptyList();

                OrderEvent orderEvent = new OrderEvent(
                        order.getId(),
                        order.getUserId(),
                        order.getTotalAmount(),
                        orderItemDtos,
                        OrderStatus.CANCELED.name(),
                        order.getExpiresAt()
                );
                orderProducer.sendOrderEvent(orderEvent);

                log.info("Order ID {} was cancelled due to non-payment.", order.getId());
            }
        } catch (Exception ex) {
            log.error("Error during cancelExpiredOrders: {}", ex.getMessage(), ex);
        }
    }

    // Update operations
    @Override
    public boolean updateOrderStatus(Long orderId, OrderStatus status) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
            if (order.getStatus() == status) {
                return false;
            }
            order.setStatus(status);
            orderRepository.save(order);
            log.info("Order ID {} updated to status: {}", orderId, status);
            return true;
        } catch (Exception ex) {
            log.error("Error updating status for order ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to update order status", ex);
        }
    }

    // Read operations
    @Override
    public OrderResponse getOrderById(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
            String paymentLink = order.getStatus() == OrderStatus.PENDING
                    ? fetchPaymentLink(order.getId(), order.getTotalAmount(), order.getUserId())
                    : null;
            return new OrderResponse(order.getId(), order.getStatus().name(), BigDecimal.valueOf(order.getTotalAmount()), paymentLink);
        } catch (Exception ex) {
            log.error("Error fetching order by ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to get order", ex);
        }
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Long userId) {
        try {
            return orderRepository.findByUserId(userId).stream()
                    .map(order -> new OrderResponse(
                            order.getId(),
                            order.getStatus().name(),
                            BigDecimal.valueOf(order.getTotalAmount()),
                            order.getStatus() == OrderStatus.PENDING
                                    ? fetchPaymentLink(order.getId(), order.getTotalAmount(), order.getUserId())
                                    : null
                    ))
                    .toList();
        } catch (Exception ex) {
            log.error("Error fetching orders for user ID {}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to get orders by user", ex);
        }
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        try {
            return orderRepository.findByStatus(status).stream()
                    .map(order -> new OrderResponse(
                            order.getId(),
                            order.getStatus().name(),
                            BigDecimal.valueOf(order.getTotalAmount()),
                            order.getStatus() == OrderStatus.PENDING
                                    ? fetchPaymentLink(order.getId(), order.getTotalAmount(), order.getUserId())
                                    : null
                    ))
                    .toList();
        } catch (Exception ex) {
            log.error("Error fetching orders by status {}: {}", status, ex.getMessage(), ex);
            throw new RuntimeException("Failed to get orders by status", ex);
        }
    }

    @Override
    public String getOrderStatus(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
            return order.getStatus().name();
        } catch (Exception ex) {
            log.error("Error getting order status for order ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to get order status", ex);
        }
    }

    @Override
    public OrderEvent getOrderEventById(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
            List<OrderItemDto> orderItemDtos = (order.getItems() != null)
                    ? order.getItems().stream()
                    .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            return new OrderEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    orderItemDtos,
                    order.getStatus().name(),
                    order.getExpiresAt()
            );
        } catch (Exception ex) {
            log.error("Error getting OrderEvent for order ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to get order event", ex);
        }
    }

    // Delete operations
    @Override
    public OrderResponse cancelOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found!"));
            if (order.getStatus() == OrderStatus.CANCELED) {
                throw new RuntimeException("Order is already cancelled!");
            }
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);

            List<OrderItemDto> orderItemDtos = (order.getItems() != null)
                    ? order.getItems().stream()
                    .map(item -> new OrderItemDto(item.getProductId(), item.getQuantity()))
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            OrderEvent orderEvent = new OrderEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    orderItemDtos,
                    OrderStatus.CANCELED.name(),
                    order.getExpiresAt()
            );
            orderProducer.sendOrderEvent(orderEvent);

            log.info("Order ID {} was manually cancelled.", orderId);

            return new OrderResponse(
                    order.getId(),
                    order.getStatus().name(),
                    BigDecimal.valueOf(order.getTotalAmount()),
                    null
            );
        } catch (Exception ex) {
            log.error("Error canceling order ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to cancel order", ex);
        }
    }

    // Other operations
    @Override
    public void restoreStock(Long productId, int quantity) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri(productServiceBaseUrl + "/products/{id}/increase-stock/{quantity}", productId, quantity)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception ex) {
            log.error("Error restoring stock for product ID {}: {}", productId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to restore stock", ex);
        }
    }

    // Helper methods
    private String fetchPaymentLink(Long orderId, Double amount, Long userId) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(paymentServiceBaseUrl + "/payments/create-payment-link")
                    .bodyValue(new CreatePaymentLinkRequestDto(orderId, amount, userId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception ex) {
            log.error("Error fetching payment link for order ID {}: {}", orderId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch payment link", ex);
        }
    }

    private boolean checkProductStock(Long productId, int quantity) {
        try {
            Integer stock = webClientBuilder.build()
                    .get()
                    .uri(productServiceBaseUrl + "/products/{id}/stock", productId)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .defaultIfEmpty(0)
                    .block();
            return stock >= quantity;
        } catch (Exception e) {
            log.error("Error checking stock for product {}: {}", productId, e.getMessage());
            return false; // Assume out of stock on error
        }
    }
}
