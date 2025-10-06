package com.theblood.identityservice.service;

import com.theblood.identityservice.dto.request.UserRequest;
import com.theblood.identityservice.dto.response.RegisterResponse;
import com.theblood.identityservice.dto.response.UserDetail;
import com.theblood.identityservice.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;
import java.util.UUID;

public interface UserService {

    UserDetailsService userDetailsService();

    RegisterResponse registerUser(UserRequest userRequest, HttpServletResponse response);

    User findByUsername(String username);

    boolean existsById(UUID id);

    User findById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<UserDetail> getListUsers(Pageable pageable);

    UserDetail getUserDetail(UUID id);

    UserDetail updateUser(UUID userId, UserRequest userRequest);

    Page<UserDetail> search(Pageable pageable, Map<String, String> criteria);

    void deleteUser(UUID id);

    void encodePassAllUsers();

}
