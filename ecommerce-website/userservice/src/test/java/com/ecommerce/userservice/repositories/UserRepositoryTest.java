package com.ecommerce.userservice.repositories;

import com.ecommerce.userservice.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test5@example.com");
        user.setUsername("testuser5");
        user.setPassword("dummy5Password");
        user.setRole("ADMIN");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test5@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("test5@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setEmail("test6@example.com");
        user.setUsername("testuser6");
        user.setPassword("dummy6Password");
        user.setRole("ADMIN");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testuser6");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser6", foundUser.get().getUsername());
    }
}
