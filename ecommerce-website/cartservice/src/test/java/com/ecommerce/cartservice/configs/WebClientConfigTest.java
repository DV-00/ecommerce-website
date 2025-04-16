package com.ecommerce.cartservice.configs;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebClientConfigTest {

    @Test
    void testWebClientBuilderBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebClientConfig.class);

        WebClient.Builder webClientBuilder = context.getBean(WebClient.Builder.class);

        assertNotNull(webClientBuilder);

        context.close();
    }
}
