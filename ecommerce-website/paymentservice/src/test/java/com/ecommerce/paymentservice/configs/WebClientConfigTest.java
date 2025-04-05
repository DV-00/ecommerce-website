package com.ecommerce.paymentservice.configs;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import static org.assertj.core.api.Assertions.assertThat;

public class WebClientConfigTest {

    @Test
    public void testWebClientBuilderBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WebClientConfig.class);
        WebClient.Builder webClientBuilder = context.getBean(WebClient.Builder.class);
        assertThat(webClientBuilder).isNotNull();
        context.close();
    }
}