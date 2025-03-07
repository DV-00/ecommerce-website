package com.ecommerce.userservice.services;

import com.ecommerce.userservice.dtos.LoginRequestDto;
import com.ecommerce.userservice.dtos.RegisterUserRequestDto;
import com.ecommerce.userservice.dtos.UserResponseDto;
import com.ecommerce.userservice.exceptions.InvalidTokenException;
import com.ecommerce.userservice.exceptions.UserNotFoundException;
import com.ecommerce.userservice.models.User;
import com.ecommerce.userservice.repositories.UserRepository;
import com.ecommerce.userservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Register new user
    @Override
    public User registerUser(RegisterUserRequestDto registerUserRequestDto) {
        User user = new User();
        user.setUsername(registerUserRequestDto.getUsername());
        user.setEmail(registerUserRequestDto.getEmail());

        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(registerUserRequestDto.getPassword());
        user.setPassword(encodedPassword);

        // Assign role, default to "CUSTOMER"
        user.setRole(registerUserRequestDto.getRole() != null ? registerUserRequestDto.getRole() : "CUSTOMER");

        return userRepository.save(user);
    }

    // ✅ Login user and generate JWT with role
    @Override
    public String loginUser(LoginRequestDto loginRequestDto) {
        loginRequestDto.validate();
        Optional<User> userOptional = Optional.empty();

        // Search by email first
        if (loginRequestDto.getEmail() != null && !loginRequestDto.getEmail().trim().isEmpty()) {
            userOptional = userRepository.findByEmail(loginRequestDto.getEmail().trim());
        }

        // Search by username if email is not provided or not found
        if (userOptional.isEmpty() && loginRequestDto.getUsername() != null && !loginRequestDto.getUsername().trim().isEmpty()) {
            userOptional = userRepository.findByUsername(loginRequestDto.getUsername().trim());
        }

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found!");
        }

        User user = userOptional.get();

        // Validate password
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid credentials!");
        }

        // Generate JWT with email + role
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    // ✅ Get user by ID
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    // ✅ Validate JWT and return user info
    @Override
    public UserResponseDto validateToken(String token) {
        String userEmail = jwtUtil.extractUsername(token);
        String userRole = jwtUtil.extractRole(token);

        // Validate both email and role
        if (!jwtUtil.validateToken(token, userEmail, userRole)) {
            throw new InvalidTokenException("Invalid or expired token!");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        return new UserResponseDto(user.getUsername(), user.getRole());
    }
}
