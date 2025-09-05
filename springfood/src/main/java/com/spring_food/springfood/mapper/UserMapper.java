package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toUser(UserRequest userRequest);
}
