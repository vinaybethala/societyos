package com.societyos.service;

import com.societyos.dto.request.LoginRequest;
import com.societyos.dto.request.RegisterRequest;
import com.societyos.dto.response.AuthResponse;
import com.societyos.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser();
}

