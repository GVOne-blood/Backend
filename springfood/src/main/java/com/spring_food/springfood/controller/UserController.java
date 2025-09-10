package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
