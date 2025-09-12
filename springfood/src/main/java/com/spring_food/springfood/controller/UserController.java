package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class UserController {

    UserService userService;

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

    @PutMapping("/")
    public ResponseEntity<ResponseData<UserDetail>> updateUser(@RequestBody UserDetail userDetail){
        UserDetail user = userService.updateUser(userDetail);
        return new ResponseEntity<>(new ResponseData<>(200, "User updated successfully", user), HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<?>> deleteUserById(@PathVariable String id, HttpServletResponse response){
        try{
            userService.deleteUser(id, response);
            return new ResponseEntity<>(new ResponseData<>(204, "User deleted ", null), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseData<>(500, "Error deleting user", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
