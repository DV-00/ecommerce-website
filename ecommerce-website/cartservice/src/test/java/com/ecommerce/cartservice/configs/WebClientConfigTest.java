package com.ecommerce.cartservice.configs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class WebClientConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void webClientBuilderBeanIsCreated() {
        WebClient.Builder webClientBuilder = applicationContext.getBean(WebClient.Builder.class);
        assertNotNull(webClientBuilder, "WebClient.Builder bean should be created and not null");
    }
}