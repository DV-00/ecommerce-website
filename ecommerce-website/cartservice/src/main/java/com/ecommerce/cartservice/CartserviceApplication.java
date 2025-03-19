package com.ecommerce.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CartserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartserviceApplication.class, args);
    }
}
