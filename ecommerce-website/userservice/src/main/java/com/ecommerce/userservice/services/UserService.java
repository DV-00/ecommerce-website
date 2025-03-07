package com.ecommerce.userservice.services;

import com.ecommerce.userservice.dtos.LoginRequestDto;
import com.ecommerce.userservice.dtos.RegisterUserRequestDto;
import com.ecommerce.userservice.dtos.UserResponseDto;
import com.ecommerce.userservice.models.User;



public interface UserService {
    User registerUser(RegisterUserRequestDto registerUserRequestDto);
    String loginUser(LoginRequestDto loginRequestDto);
    User getUserById(Long id);
    UserResponseDto validateToken(String token);
}
