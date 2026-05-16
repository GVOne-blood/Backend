package com.theblood.authentication.controller;


import com.theblood.authentication.dto.request.UserRequest;
import com.theblood.authentication.dto.response.RegisterResponse;
import com.theblood.authentication.dto.response.TokenResponse;
import com.theblood.authentication.service.AuthService;
import com.theblood.authentication.service.JwtService;
import com.theblood.authentication.service.UserService;
import com.theblood.springfood.common.dto.response.LoginRequest;
import com.theblood.springfood.common.dto.response.ResponseData;
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

    /**
     * PUBLIC API - DEV ONLY (đồ án sinh viên)
     * Register user với role chỉ định.
     * 
     * Body example:
     * {
     *   "firstName": "Admin",
     *   "lastName": "User",
     *   "username": "admin",
     *   "password": "Admin@123",
     *   "email": "admin@springfood.vn",
     *   "gender": "MALE",
     *   "phone": "0912345678"
     * }
     * 
     * Query param: ?role=ADMIN (CUSTOMER|SHOP_OWNER|ADMIN|STAFF|DELIVER)
     */
    @PostMapping("/register-with-role")
    public ResponseEntity<ResponseData<RegisterResponse>> registerWithRole(
            @Valid @RequestBody UserRequest userRequest,
            @RequestParam(name = "role", defaultValue = "CUSTOMER") String role,
            HttpServletResponse response) {

        RegisterResponse res = userService.registerUserWithRole(userRequest, role, response);
        return new ResponseEntity<>(
                new ResponseData<>(201, "User registered with role " + role.toUpperCase() + " successfully", res),
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
    public ResponseEntity<ResponseData<TokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        // Đọc REFRESH_TOKEN từ HttpOnly cookie (FE không có quyền đọc/gửi qua body)
        String refreshToken = com.theblood.springfood.common.util.CookieUtil
                .getElementFromCookie(request, com.theblood.springfood.common.enums.CookieKey.REFRESH_TOKEN.name());

        TokenResponse tokenResponse = authService.refresh(refreshToken);

        // Set-Cookie ACCESS_TOKEN mới (15 phút). REFRESH_TOKEN giữ nguyên cho tới khi logout/expire.
        response.addCookie(com.theblood.springfood.common.util.CookieUtil.createCookie(
                com.theblood.springfood.common.enums.CookieKey.ACCESS_TOKEN.name(),
                tokenResponse.getAccessToken(),
                15 * 60));

        return ResponseEntity.ok(new ResponseData<>(200, "Token refreshed successfully", tokenResponse));
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