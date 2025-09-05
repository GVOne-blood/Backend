package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.model.ENUM.UserStatus;
import com.spring_food.springfood.common.util.PasswordEncoderUtil;
import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.mapper.UserMapper;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.service.JwtService;
import com.spring_food.springfood.service.UserService;
import com.spring_food.springfood.model.ENUM.TokenType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    UserMapper userMapper;
    
    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public RegisterResponse registerUser(UserRequest userRequest) {
        // Check if username already exists
        if (existsByUsername(userRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if email already exists
        if (existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = userMapper.toUser(userRequest);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        // Set default values
        user.setStatus(UserStatus.ACTIVE);
        user.setIsDeleted(false);
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        // Generate JWT tokens
        String accessToken = jwtService.generateToken(TokenType.ACCESS_TOKEN, savedUser);
        String refreshToken = jwtService.generateToken(TokenType.REFRESH_TOKEN, savedUser);

        // Build response
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getTokenExpiration(TokenType.ACCESS_TOKEN))
                .build();
    }
    
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void encodePassAllUsers(){
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (!user.getPassword().startsWith("$2a$10") && !user.getPassword().isEmpty()) {
                String encodedPassword = PasswordEncoderUtil.encode(user.getPassword());
                user.setPassword(encodedPassword);
                userRepository.save(user);
            }
        }
    }
}
