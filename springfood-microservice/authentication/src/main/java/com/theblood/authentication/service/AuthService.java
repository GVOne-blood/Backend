package com.theblood.authentication.service;

import com.theblood.authentication.dto.response.TokenResponse;
import com.theblood.common.dto.response.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    TokenResponse login(LoginRequest loginRequest, HttpServletResponse response);

    TokenResponse refresh(String request);

    void logout(HttpServletRequest request, HttpServletResponse response);

}
