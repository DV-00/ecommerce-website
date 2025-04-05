package com.ecommerce.productservice.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import static org.junit.jupiter.api.Assertions.*;

class RedisConfigTest {

    private RedisConfig redisConfig;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisConfig = new RedisConfig();
    }

    @Test
    void testRedisConnectionFactory() {
        RedisConnectionFactory factory = redisConfig.redisConnectionFactory();
        assertNotNull(factory);
        assertInstanceOf(LettuceConnectionFactory.class, factory);
    }

    @Test
    void testRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(redisConnectionFactory);

        assertNotNull(redisTemplate);
        assertEquals(redisConnectionFactory, redisTemplate.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, redisTemplate.getValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getHashKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, redisTemplate.getHashValueSerializer());
    }
}
