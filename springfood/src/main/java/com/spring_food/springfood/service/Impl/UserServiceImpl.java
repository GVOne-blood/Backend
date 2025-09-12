package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.common.enums.TokenType;
import com.spring_food.springfood.common.enums.UserStatus;
import com.spring_food.springfood.common.util.PasswordEncoderUtil;
import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.mapper.UserMapper;
import com.spring_food.springfood.model.Role;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.model.UserHasRole;
import com.spring_food.springfood.repository.RoleRepository;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.service.AuthService;
import com.spring_food.springfood.service.JwtService;
import com.spring_food.springfood.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    AuthService authService;
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
            throw new InvalidDataException("Username is already taken!");
        }
        
        // Check if email already exists
        if (existsByEmail(userRequest.getEmail())) {
            throw new InvalidDataException("Email is already in use!");
        }
        
        // Create new user
        User user = userMapper.toUser(userRequest);
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        // Set default values
        user.setStatus(UserStatus.ACTIVE);
        user.setIsDeleted(false);

        UserHasRole userHasRole = new UserHasRole();
        userHasRole.setUser(user);
        Optional<Role> role = roleRepository.findById("CUSTOMER");
        if (role.isEmpty()) throw new InvalidDataException("Role is empty!");
        userHasRole.setRole(role.get());
        userHasRole.setUser(user);
        user.getUserRoles().add(userHasRole);


        // Save user to database
        User savedUser = userRepository.save(user);

        //send confirm mail

        // Generate JWT tokens
        String accessToken = jwtService.generateToken(TokenType.ACCESS, savedUser);
        String refreshToken = jwtService.generateToken(TokenType.REFRESH, savedUser);

        // Build response
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getTokenExpiration(TokenType.ACCESS))
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
    public List<UserDetail> getListUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toUserDetail(users);
    }

    @Override
    public UserDetail getUserDetail(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toUserDetail(user);
    }

    @Override
    public UserDetail updateUser(UserDetail userDetail) {
        return null;
    }

    @Override
    public void deleteUser(String id, HttpServletResponse response) {
        if (!userRepository.existsById(id)) {
            throw new InvalidDataException("User not found with id: " + id);
        }
        authService.logout(response);
        userRepository.deleteById(id);
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
