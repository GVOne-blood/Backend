package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.model.ENUM.Gender;
import com.spring_food.springfood.common.util.EnumPattern;
import com.spring_food.springfood.common.util.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 30, message = "First name must be at least 2 characters")
    String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 30, message = "Last name must be at least 2 characters")
    String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be at least 3 characters")
    String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character and no whitespace")
    String password;
    
    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Email should be valid")
    String email;
    
    @EnumPattern(name = "gender", regexp = ("MALE|FEMALE|OTHER"))
    Gender gender;

    @PhoneNumber(message = "Phone number must be valid")
    String phone;
    
    String address;
}
