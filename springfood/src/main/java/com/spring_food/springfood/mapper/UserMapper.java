package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toUser(UserRequest userRequest);

    @Mapping(target = "id", ignore = false)
    UserDetail toUserDetail(User user);

    @Mapping(target = "", ignore = true)
    List<UserDetail> toUserDetail(List<User> user);
}
