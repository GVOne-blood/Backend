package com.spring_food.springfood.common.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

        @Override
        public void initialize(PhoneNumber phoneNumberNo) {
            // hàm void initialize định nghĩa các giá trị mặc định cho annotation
        }

        @Override
        public boolean isValid(String phone, ConstraintValidatorContext constraintValidatorContext) {
            if (phone == null) {
                return false;
            }
            if (phone.matches("^\\d{10}$")) {
                return true;
            }
            else if (phone.matches("^\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}"))
                return true;
            else if (phone.matches("^\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}"))
                return true;
            else return  (phone.matches("^\\(\\d{3}\\)-\\d{3}-\\d{4}"));
        }
}
