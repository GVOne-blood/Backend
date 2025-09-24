package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

public interface UserService {

    boolean isCurrentUser(String userId);

    UserDetailsService userDetailsService();

    RegisterResponse registerUser(UserRequest userRequest, HttpServletResponse response);

    User findByUsername(String username);

    User findById(String id);

    boolean existsById(String id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<UserDetail> getListUsers(Pageable pageable);

    UserDetail getUserDetail(String id);

    UserDetail updateUser(String userId, UserRequest userRequest);

    Page<UserDetail> search(Pageable pageable, Map<String, String> criteria);

    void deleteUser(String id);

    void encodePassAllUsers();

}
