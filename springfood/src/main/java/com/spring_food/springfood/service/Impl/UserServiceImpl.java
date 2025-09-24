package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.common.enums.CookieKey;
import com.spring_food.springfood.common.enums.TokenType;
import com.spring_food.springfood.common.enums.UserStatus;
import com.spring_food.springfood.common.util.CookieUtil;
import com.spring_food.springfood.common.util.PasswordEncoderUtil;
import com.spring_food.springfood.dto.request.SearchCriteria;
import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.mapper.UserMapper;
import com.spring_food.springfood.model.Cart;
import com.spring_food.springfood.model.Role;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.model.UserHasRole;
import com.spring_food.springfood.repository.RoleRepository;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.service.JwtService;
import com.spring_food.springfood.service.UserService;
import com.spring_food.springfood.specification.SearchSpecification;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    UserMapper userMapper;

    @Override
    public boolean isCurrentUser(String userId) {
        return false;
    }

    @Override
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public RegisterResponse registerUser(UserRequest userRequest, HttpServletResponse response) {
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

        Cart cart = new Cart();
        user.setCart(cart);
        cart.setUser(user);

        // Save user to database
        User savedUser = userRepository.save(user);

        //send confirm mail

        // Generate JWT tokens
        String accessToken = jwtService.generateToken(TokenType.ACCESS, savedUser);
        String refreshToken = jwtService.generateToken(TokenType.REFRESH, savedUser);

        // Tạo Access Token Cookie
        response.addCookie(CookieUtil.createCookie(CookieKey.ACCESS_TOKEN.name(), accessToken, 15 * 60));

        // Tạo Refresh Token Cookie
        response.addCookie(CookieUtil.createCookie(CookieKey.REFRESH_TOKEN.name(), refreshToken, 7 * 24 * 60 * 60));

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
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("User not found with id: " + id));
    }

    @Override
    public boolean existsById(String id) {
        return userRepository.existsById(id);
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
    public Page<UserDetail> getListUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserDetail);

    }

    @Override
    public UserDetail getUserDetail(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toUserDetail(user);
    }


    @PreAuthorize("hasRole('CUSTOMER')")
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public UserDetail updateUser(String userId, UserRequest userRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidDataException("User not found with id: " + userId));

        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(user.getEmail())) {
            if (existsByEmail(userRequest.getEmail())) {
                throw new InvalidDataException("Email is already in use by another user!");
            }
        }

        // Sử dụng MapStruct để update, chỉ các field non-null sẽ được update, username và password are rejected
        userMapper.toUser(user, userRequest);

        // User updatedUser = userRepository.save(user);

        return userMapper.toUserDetail(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public Page<UserDetail> search(Pageable pageable, Map<String, String> criteria) {

        if (criteria.isEmpty()) {
            getListUsers(pageable);
        }

        Pattern pattern = Pattern.compile("^(!=|<=|>=|[:=<>~])(.+)$");

        Specification<User> spec = null;
        List<SearchCriteria> searchParams = new ArrayList<>();

        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("page") || key.equals("size") || key.equals("sort")) continue;

            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                SearchCriteria searchCriteria = new SearchCriteria();
                searchCriteria.setKeyword(key);
                searchCriteria.setOperation(matcher.group(1));
                searchCriteria.setValue(matcher.group(2));
                searchParams.add(searchCriteria);
            }

            for (SearchCriteria params : searchParams) {
                Specification<User> currentSpec = SearchSpecification.buildSpecification(params);

                if (spec == null)
                    spec = currentSpec;
                else {
                    spec = spec.and(currentSpec);
                }
            }

            if (spec == null) getListUsers(pageable);
            Page<User> res = userRepository.findAll(spec, pageable);
            return res.map(userMapper::toUserDetail);

        }
        return null;
    }


    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("User not found with id: " + id));

        // Soft delete - chỉ set isDeleted = true
        // Với @SQLDelete annotation, khi gọi delete sẽ tự động chạy UPDATE thay vì DELETE
        userRepository.delete(user);
        // user.setIsDeleted(true);
        // user.setStatus(UserStatus.INACTIVE);
        // userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN') and hasAuthority('user:encode')")
    @Override
    public void encodePassAllUsers() {
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
