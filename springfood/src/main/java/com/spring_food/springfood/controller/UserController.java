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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class UserController {

    UserService userService;
    AuthService authService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<List<UserDetail>>> listUsers(){

        List<UserDetail> users = userService.getListUsers();

        return new ResponseEntity<>
                (new ResponseData<>(HttpStatus.OK.value(),
                        "Get list users successfully",
                        users), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserDetail>> getUserById(@PathVariable String id){

        UserDetail user = userService.getUserDetail(id);

        return new ResponseEntity<>
                (new ResponseData<>(HttpStatus.OK.value(),
                        "Get user detail successfully",
                        user), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<UserDetail>> updateUser(@PathVariable(value = "id") String id, @RequestBody UserRequest userRequest){
        UserDetail user = userService.updateUser(id, userRequest);
        return new ResponseEntity<>(new ResponseData<>(200, "User updated successfully", user), HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<?>> deleteUserById(
            @PathVariable String id, 
            HttpServletResponse response,
            @AuthenticationPrincipal
            UserDetails currentUser){
        try{

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
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseData<>(500, "Error deleting user: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
