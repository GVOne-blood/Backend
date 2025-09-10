package com.spring_food.springfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetail {
    String id;
    String username;
    String firstName;
    String lastName;
    String email;
    String phone;
    String address;
    String avatar;
    LocalDate dob;
}
