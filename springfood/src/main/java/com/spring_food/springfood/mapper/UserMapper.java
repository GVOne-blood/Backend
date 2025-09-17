package com.spring_food.springfood.mapper;

import com.spring_food.springfood.dto.request.UserRequest;
import com.spring_food.springfood.dto.response.UserDetail;
import com.spring_food.springfood.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    
    @Mapping(target = "password", ignore = true)
    User toUser(UserRequest userRequest);
    
    /**
     * Update existing user with data from UserRequest
     * Only non-null values will be updated
     * Password and username are ignored for security
     */
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "username", ignore = true) 
    void toUser(@MappingTarget User user, UserRequest userRequest);

    @Mapping(target = "id", ignore = false)
    UserDetail toUserDetail(User user);

    @Mapping(target = "", ignore = true)
    List<UserDetail> toUserDetail(List<User> user);
}
