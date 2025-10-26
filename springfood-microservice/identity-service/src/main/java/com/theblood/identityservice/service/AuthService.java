package com.theblood.identityservice.service;

import com.theblood.common.dto.response.LoginRequest;
import com.theblood.identityservice.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    TokenResponse login(LoginRequest loginRequest, HttpServletResponse response);

    TokenResponse refresh(String request);

    void logout(HttpServletRequest request, HttpServletResponse response);

}
