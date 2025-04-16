package com.ecommerce.userservice.controllers;

import com.ecommerce.userservice.dtos.LoginRequestDto;
import com.ecommerce.userservice.dtos.RegisterUserRequestDto;
import com.ecommerce.userservice.dtos.UserResponseDto;
import com.ecommerce.userservice.exceptions.InvalidTokenException;
import com.ecommerce.userservice.models.User;
import com.ecommerce.userservice.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.TestConfig.class)
public class UserControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterUser() throws Exception {
        RegisterUserRequestDto registerDto = new RegisterUserRequestDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("password");
        registerDto.setEmail("test@example.com");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userService.registerUser(any(RegisterUserRequestDto.class))).thenReturn(user);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testLoginUser() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("password");

        String token = "dummyToken";
        when(userService.loginUser(any(LoginRequestDto.class))).thenReturn(token);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }

    @Test
    public void testValidateToken_Valid() throws Exception {
        String token = "validToken";
        UserResponseDto userResponse = new UserResponseDto(1L, "testuser", "USER");
        when(userService.validateToken(token)).thenReturn(userResponse);

        mockMvc.perform(get("/users/validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    public void testValidateToken_Invalid() throws Exception {
        String token = "invalidToken";
        when(userService.validateToken(token)).thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(get("/users/validate")
                        .param("token", token))
                .andExpect(status().isUnauthorized());
    }
}
