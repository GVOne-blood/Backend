package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.LoginRequest;
import com.spring_food.springfood.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    TokenResponse login (LoginRequest loginRequest, HttpServletResponse response);
    TokenResponse refresh(HttpServletRequest request);
    void logout(HttpServletResponse response);

}
