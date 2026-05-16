package com.theblood.authentication.service;

import com.theblood.authentication.dto.request.UserRequest;
import com.theblood.authentication.dto.response.RegisterResponse;
import com.theblood.authentication.dto.response.UserDetail;
import com.theblood.authentication.model.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;
import java.util.UUID;

public interface UserService {

    UserDetailsService userDetailsService();

    RegisterResponse registerUser(UserRequest userRequest, HttpServletResponse response);

    /**
     * Register user với role được chỉ định (CUSTOMER, SHOP_OWNER, ADMIN, STAFF, DELIVER).
     * PUBLIC API - chỉ dùng cho dev/đồ án.
     */
    RegisterResponse registerUserWithRole(UserRequest userRequest, String roleName, HttpServletResponse response);

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
