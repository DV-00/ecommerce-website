package com.ecommerce.cartservice.kafka;

import com.ecommerce.cartservice.dtos.CartClearEvent;
import com.ecommerce.cartservice.services.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

class CartConsumerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartConsumer cartConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleCartClearEventSuccessfully() {

        CartClearEvent cartClearEvent = new CartClearEvent(1L);

        cartConsumer.handleCartClearEvent(cartClearEvent);

        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    void shouldNotCallClearCartForNullUserId() {

        CartClearEvent cartClearEvent = new CartClearEvent(null);

        cartConsumer.handleCartClearEvent(cartClearEvent);

        verify(cartService, never()).clearCart(anyLong());
    }
}
