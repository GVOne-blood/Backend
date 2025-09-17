package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.RegisterResponse;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    UserDetailsService userDetailsService();
    RegisterResponse registerUser(UserRequest userRequest);
    User findByUsername(String username);
    User findById(String id);

    boolean existsById(String id);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserDetail> getListUsers();
    UserDetail getUserDetail(String id);
    UserDetail updateUser(String userId, UserRequest userRequest);
    void deleteUser(String id);
    void encodePassAllUsers();

}
