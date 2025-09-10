package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.LoginRequest;
import com.spring_food.springfood.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    TokenResponse login (LoginRequest loginRequest);
    TokenResponse refresh(HttpServletRequest request);

}
