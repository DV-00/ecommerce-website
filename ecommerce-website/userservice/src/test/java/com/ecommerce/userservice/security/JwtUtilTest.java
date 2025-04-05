package com.ecommerce.userservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import javax.crypto.SecretKey;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    public void testGenerateTokenAndExtractClaims() {
        String username = "test@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        String extractedUsername = jwtUtil.extractUsername(token);
        String extractedRole = jwtUtil.extractRole(token);

        assertEquals(username, extractedUsername, "Extracted username should match the original");
        assertEquals(role, extractedRole, "Extracted role should match the original");
    }

    @Test
    public void testValidateTokenValid() {
        String username = "test@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        assertTrue(jwtUtil.validateToken(token, username, role), "Token should be valid for correct username and role");
    }

    @Test
    public void testValidateTokenInvalidEmail() {
        String username = "test@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        assertFalse(jwtUtil.validateToken(token, "wrong@example.com", role), "Token validation should fail for incorrect email");
    }

    @Test
    public void testValidateTokenInvalidRole() {
        String username = "test@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        assertFalse(jwtUtil.validateToken(token, username, "CUSTOMER"), "Token validation should fail for incorrect role");
    }

    @Test
    public void testExpiredToken() {
        String username = "test@example.com";
        String role = "ADMIN";

        SecretKey secretKey = (SecretKey) ReflectionTestUtils.getField(jwtUtil, "SECRET_KEY");

        String expiredToken = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000))  // expired 1 second ago
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(expiredToken, username, role),
                "Expired token should throw ExpiredJwtException");
    }
}
