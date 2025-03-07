package com.ecommerce.userservice.dtos;


public class UserResponseDto {
    private String username;
    private String role;

    public UserResponseDto(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
