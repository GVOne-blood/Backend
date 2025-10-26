package com.theblood.identityservice.controller;


import com.theblood.common.dto.request.TokenRefreshRequest;
import com.theblood.common.dto.response.LoginRequest;
import com.theblood.common.dto.response.ResponseData;
import com.theblood.identityservice.dto.request.UserRequest;
import com.theblood.identityservice.dto.response.RegisterResponse;
import com.theblood.identityservice.dto.response.TokenResponse;
import com.theblood.identityservice.service.AuthService;
import com.theblood.identityservice.service.JwtService;
import com.theblood.identityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AuthController {
    UserService userService;
    JwtService jwtService;
    AuthService authService;
    AuthenticationManager authenticationManager;

    // plain_password_for_dev
    @PostMapping("/register")
    public ResponseEntity<ResponseData<RegisterResponse>> register(@Valid @RequestBody UserRequest userRequest, HttpServletResponse response) {

        RegisterResponse res = userService.registerUser(userRequest, response);
        return new ResponseEntity<>(
                new ResponseData<>(201, "User registered successfully", res),
                HttpStatus.OK
        );
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
            e.printStackTrace();
            return new ResponseEntity<>(
                    new ResponseData<>(400, e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody TokenRefreshRequest request) {  // ✅ Nhận từ body

        TokenResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseData<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(loginRequest, response);

        return new ResponseEntity<>(
                new ResponseData<>(200, "Login successful", tokenResponse),
                HttpStatus.OK
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<ResponseData<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        // blacklist with redis
        authService.logout(request, response);
        return ResponseEntity.ok(new ResponseData<>(204, "Logout successful", null));
    }
}