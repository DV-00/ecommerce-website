package com.ecommerce.cartservice.repositories;

import com.ecommerce.cartservice.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    List<CartItem> findBySessionId(String sessionId);

    Integer countByUserId(Long userId);

    void deleteByUserId(Long userId);

}
