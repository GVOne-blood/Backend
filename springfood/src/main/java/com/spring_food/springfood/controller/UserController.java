package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserRepository userRepository;

    @GetMapping("/list")
    public ResponseEntity<ResponseData<List<User>>> listUsers(){

        List<User> users = userRepository.findAll();

        return new ResponseEntity<>
                (new ResponseData<List<User>>(HttpStatus.OK.value(),
                        "Get list users successfully",
                        users), HttpStatus.OK);
    }

}
