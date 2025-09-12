package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    UserDetailsService userDetailsService();
    RegisterResponse registerUser(UserRequest userRequest);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserDetail> getListUsers();
    UserDetail getUserDetail(String id);
    UserDetail updateUser(UserDetail userDetail);
    void deleteUser(String id, HttpServletResponse response);
    void encodePassAllUsers();

}
