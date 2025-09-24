package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.AuthService;
import com.spring_food.springfood.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class UserController {

    UserService userService;
    AuthService authService;


    @GetMapping("/")
    public ResponseEntity<ResponseData<Page<UserDetail>>> listUsers(
            @PageableDefault Pageable pageable
    ) {

        Page<UserDetail> users = userService.getListUsers(pageable);

        return new ResponseEntity<>
                (new ResponseData<>(HttpStatus.OK.value(),
                        "Get list users successfully",
                        users), HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseData<UserDetail>> getUserById(@AuthenticationPrincipal User user) {

        UserDetail resUser = userService.getUserDetail(user.getId());

        return new ResponseEntity<>
                (new ResponseData<>(HttpStatus.OK.value(),
                        "Get user detail successfully",
                        resUser), HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseData<UserDetail>> updateUser(@AuthenticationPrincipal User user, @RequestBody UserRequest userRequest) {
        UserDetail resUser = userService.updateUser(user.getId(), userRequest);
        return new ResponseEntity<>(new ResponseData<>(200, "User updated successfully", resUser), HttpStatus.OK);

    }

    @DeleteMapping("/profile/{id}")
    public ResponseEntity<ResponseData<?>> deleteUserById(
            @PathVariable String id,
            HttpServletResponse response,
            @AuthenticationPrincipal
            UserDetails currentUser) {
        try {

            User userToDelete = userService.findById(id);

            boolean isSelfDelete = currentUser != null &&
                    currentUser.getUsername() != null &&
                    userToDelete.getUsername().equals(currentUser.getUsername());

            userService.deleteUser(id);

            if (isSelfDelete) {
                authService.logout(response);
                return new ResponseEntity<>(new ResponseData<>(204, "Your account has been deleted", null), HttpStatus.OK);
            }

            return new ResponseEntity<>(new ResponseData<>(204, "User deleted successfully", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseData<>(500, "Error deleting user: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseData<Page<UserDetail>>> searchUsers(
            @PageableDefault(size = 5, page = 0, sort = "updated_at", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Map<String, String> criteria
    ) {
        try {
            Page<UserDetail> results;
            results = userService.search(pageable, criteria);
            return ResponseEntity.ok(
                    new ResponseData<>(200, "Search users successfully", results)
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseData<>(400, "Search failed: " + e.getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}
