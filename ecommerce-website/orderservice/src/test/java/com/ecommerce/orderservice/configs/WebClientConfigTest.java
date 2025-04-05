package com.ecommerce.orderservice.configs;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WebClientConfigTest {

    @Test
    public void testWebClientBuilderBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebClientConfig.class);

        WebClient.Builder builder = context.getBean(WebClient.Builder.class);

        assertNotNull(builder, "WebClient.Builder bean should not be null");

        context.close();
    }
}
