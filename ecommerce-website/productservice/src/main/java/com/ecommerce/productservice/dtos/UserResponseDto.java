package com.ecommerce.productservice.dtos;

public class UserResponseDto {
    private Long id;
    private String role;

    // Constructors
    public UserResponseDto() {}

    public UserResponseDto(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }
}
