package com.insurance.platform.service;

import com.insurance.platform.dto.AuthResponse;
import com.insurance.platform.dto.LoginRequest;
import com.insurance.platform.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
