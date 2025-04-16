package com.ecommerce.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderserviceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Configuration
    static class TestConfig {
        @Bean
        public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory() {
            return new ConcurrentKafkaListenerContainerFactory<>();
        }
    }
}