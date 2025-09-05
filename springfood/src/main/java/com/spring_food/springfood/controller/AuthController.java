package com.spring_food.springfood.controller;

import com.spring_food.springfood.model.ENUM.TokenType;
import com.spring_food.springfood.dto.request.LoginRequest;
import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.dto.response.TokenResponse;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.JwtService;
import com.spring_food.springfood.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ResponseData<RegisterResponse>> register(@Valid @RequestBody UserRequest userRequest) {
        try {
            RegisterResponse response = userService.registerUser(userRequest);
            return new ResponseEntity<>(
                    new ResponseData<>(201, "User registered successfully", response), 
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, e.getMessage(), null), 
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    @PatchMapping("/encode")
    public ResponseEntity<ResponseData<String>> encodeDumpPassword() {
        try {
            userService.encodePassAllUsers();
            return new ResponseEntity<>(
                    new ResponseData<>(200, "Password encoded successfully", null),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<ResponseData<TokenResponse>> refreshToken(){
        return null;
    }
    
    @PostMapping("/login")
    public ResponseEntity<ResponseData<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            User user = userService.findByUsername(loginRequest.getUsername());
            String accessToken = jwtService.generateToken(TokenType.ACCESS_TOKEN, user);
            String refreshToken = jwtService.generateToken(TokenType.REFRESH_TOKEN, user);
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(accessToken);
            tokenResponse.setRefreshToken(refreshToken);
            tokenResponse.setUserId(user.getId());
            tokenResponse.setUsername(user.getUsername());

            return new ResponseEntity<>(
                    new ResponseData<>(200, "Login successful", tokenResponse),
                    HttpStatus.OK
            );
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    new ResponseData<>(401, "Invalid credentials", null),
                    HttpStatus.OK
            );
        }
    }


}
