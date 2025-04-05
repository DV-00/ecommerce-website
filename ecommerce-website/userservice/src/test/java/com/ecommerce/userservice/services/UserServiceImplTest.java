package com.ecommerce.userservice.services;

import com.ecommerce.userservice.dtos.LoginRequestDto;
import com.ecommerce.userservice.dtos.RegisterUserRequestDto;
import com.ecommerce.userservice.dtos.UserResponseDto;
import com.ecommerce.userservice.exceptions.InvalidTokenException;
import com.ecommerce.userservice.exceptions.UserNotFoundException;
import com.ecommerce.userservice.models.User;
import com.ecommerce.userservice.repositories.UserRepository;
import com.ecommerce.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private RegisterUserRequestDto registerDto;
    private LoginRequestDto loginDto;
    private User sampleUser;

    @BeforeEach
    public void setUp() {
        // Set up registration DTO
        registerDto = new RegisterUserRequestDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password");

        loginDto = new LoginRequestDto();
        loginDto.setUsername("testuser");
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password");

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@example.com");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole("ADMIN"); // Must match allowed roles (CUSTOMER or ADMIN)
    }

    @Test
    public void testRegisterUser() {
        // Simulate password encoding and saving the user.
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User registeredUser = userServiceImpl.registerUser(registerDto);

        assertNotNull(registeredUser);
        assertEquals(sampleUser.getId(), registeredUser.getId());
        assertEquals(sampleUser.getEmail(), registeredUser.getEmail());
        assertEquals(sampleUser.getUsername(), registeredUser.getUsername());
        assertEquals(sampleUser.getRole(), registeredUser.getRole());
    }

    @Test
    public void testLoginUser_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(loginDto.getPassword(), sampleUser.getPassword())).thenReturn(true);

        // Simulate JWT generation
        String dummyToken = "dummy.jwt.token";
        when(jwtUtil.generateToken(sampleUser.getEmail(), sampleUser.getRole())).thenReturn(dummyToken);

        String token = userServiceImpl.loginUser(loginDto);

        assertNotNull(token);
        assertEquals(dummyToken, token);
    }

    @Test
    public void testLoginUser_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userServiceImpl.loginUser(loginDto));
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User user = userServiceImpl.getUserById(1L);

        assertNotNull(user);
        assertEquals(sampleUser.getId(), user.getId());
        assertEquals(sampleUser.getEmail(), user.getEmail());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.getUserById(1L));
    }

    @Test
    public void testValidateToken_Success() {
        String token = "dummy.jwt.token";

        when(jwtUtil.extractUsername(token)).thenReturn(sampleUser.getEmail());
        when(jwtUtil.extractRole(token)).thenReturn(sampleUser.getRole());
        when(jwtUtil.validateToken(token, sampleUser.getEmail(), sampleUser.getRole())).thenReturn(true);
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));

        UserResponseDto responseDto = userServiceImpl.validateToken(token);

        assertNotNull(responseDto);
        assertEquals(sampleUser.getId(), responseDto.getId());
        assertEquals(sampleUser.getUsername(), responseDto.getUsername());
        assertEquals(sampleUser.getRole(), responseDto.getRole());
    }

    @Test
    public void testValidateToken_Invalid() {
        String token = "dummy.jwt.token";

        when(jwtUtil.extractUsername(token)).thenReturn(sampleUser.getEmail());
        when(jwtUtil.extractRole(token)).thenReturn(sampleUser.getRole());
        when(jwtUtil.validateToken(token, sampleUser.getEmail(), sampleUser.getRole())).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> userServiceImpl.validateToken(token));
    }
}
