package com.ecommerce.userservice.controllers;

import com.ecommerce.userservice.dtos.LoginRequestDto;
import com.ecommerce.userservice.dtos.RegisterUserRequestDto;
import com.ecommerce.userservice.dtos.UserResponseDto;
import com.ecommerce.userservice.models.User;
import com.ecommerce.userservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterUserRequestDto registerUserRequestDto) {
        return ResponseEntity.ok(userService.registerUser(registerUserRequestDto));
    }


    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDTO) {
        return ResponseEntity.ok(userService.loginUser(loginRequestDTO));
    }

    @GetMapping("/validate")
    public ResponseEntity<UserResponseDto> validateToken(@RequestParam String token) {
        UserResponseDto userResponse = userService.validateToken(token);
        return ResponseEntity.ok(userResponse);
    }
}
