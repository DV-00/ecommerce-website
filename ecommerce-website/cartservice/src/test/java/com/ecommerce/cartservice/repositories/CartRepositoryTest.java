package com.ecommerce.cartservice.repositories;

import com.ecommerce.cartservice.models.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ActiveProfiles("test")
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    private CartItem cartItem;

    @BeforeEach
    public void setup() {
        cartRepository.deleteAll();

        cartItem = new CartItem();
        cartItem.setUserId(1L);
        cartItem.setSessionId("session123");
        cartItem.setProductId(101L);
        cartItem.setQuantity(2);
        cartRepository.save(cartItem);
    }

    @AfterEach
    public void cleanup() {
        cartRepository.deleteAll();
    }

    @Test
    public void testFindByUserId() {
        List<CartItem> items = cartRepository.findByUserId(1L);
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    public void testFindBySessionId() {
        List<CartItem> items = cartRepository.findBySessionId("session123");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0).getSessionId()).isEqualTo("session123");
    }

    @Test
    public void testCountByUserId() {
        Integer count = cartRepository.countByUserId(1L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testDeleteByUserId() {
        cartRepository.deleteByUserId(1L);
        List<CartItem> items = cartRepository.findByUserId(1L);
        assertThat(items).isEmpty();
    }
}