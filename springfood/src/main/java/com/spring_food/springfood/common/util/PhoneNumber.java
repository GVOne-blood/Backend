package com.spring_food.springfood.common.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber{
    String message() default "Phone number format invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
