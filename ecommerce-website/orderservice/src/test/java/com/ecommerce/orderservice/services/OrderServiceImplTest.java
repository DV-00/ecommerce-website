package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.dtos.OrderEvent;
import com.ecommerce.orderservice.dtos.OrderResponse;
import com.ecommerce.orderservice.kafka.OrderProducer;
import com.ecommerce.orderservice.models.Order;
import com.ecommerce.orderservice.models.OrderStatus;
import com.ecommerce.orderservice.repositories.OrderItemRepository;
import com.ecommerce.orderservice.repositories.OrderRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceImplTest {

    private static MockWebServer mockWebServer;
    private WebClient.Builder webClientBuilder;
    private WebClient webClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderProducer orderProducer;

    private OrderServiceImpl orderService;

    @BeforeClass
    public static void setupAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterClass
    public static void tearDownAll() throws IOException {
        mockWebServer.shutdown();
    }

    @Before
    public void setUp() {
        String baseUrl = mockWebServer.url("/").toString();
        webClientBuilder = WebClient.builder().baseUrl(baseUrl);
        webClient = webClientBuilder.build();

        orderService = new OrderServiceImpl(orderRepository, webClientBuilder, orderItemRepository, orderProducer);
    }

    @Test
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING);
        order1.setCreatedAt(now.minusMinutes(11));
        order1.setTotalAmount(100.0);

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(order1));

        orderService.cancelExpiredOrders();

        verify(orderRepository, times(1))
                .findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).sendOrderEvent(any(OrderEvent.class));
    }

    @Test
    public void updateOrderStatus() {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.COMPLETED;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        boolean result = orderService.updateOrderStatus(orderId, newStatus);

        assertTrue(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void getOrderById() throws Exception {
        Long orderId = 1L;
        Long userId = 123L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(100.0);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Mock WebClient call for payment link
        mockWebServer.enqueue(new MockResponse()
                .setBody("\"http://mocked-payment-link.com\"") // Correct JSON format
                .addHeader("Content-Type", "application/json"));

        OrderResponse response = orderService.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(OrderStatus.PENDING.name(), response.getStatus());
        assertEquals(BigDecimal.valueOf(100.0), response.getTotalAmount());
        assertEquals("http://mocked-payment-link.com", response.getPaymentLink().replaceAll("^\"|\"$", ""));

    }

    @Test
    public void getOrdersByUser() throws Exception {
        Long userId = 1L;

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING);
        order1.setTotalAmount(100.0);
        order1.setUserId(userId);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.COMPLETED);
        order2.setTotalAmount(200.0);
        order2.setUserId(userId);

        when(orderRepository.findByUserId(userId)).thenReturn(Arrays.asList(order1, order2));

        // Mock payment link for PENDING order
        mockWebServer.enqueue(new MockResponse()
                .setBody("\"http://mocked-payment-link.com\"") // Correct JSON format
                .addHeader("Content-Type", "application/json"));

        List<OrderResponse> responses = orderService.getOrdersByUser(userId);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }
}
