package com.ecommerce.userservice.dtos;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {

    private String email;
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructor
    public LoginRequestDto() {}

    // Getters
    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Validation method
    public void validate() {
        if ((email == null || email.trim().isEmpty()) && (username == null || username.trim().isEmpty())) {
            throw new IllegalArgumentException("Email or Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
