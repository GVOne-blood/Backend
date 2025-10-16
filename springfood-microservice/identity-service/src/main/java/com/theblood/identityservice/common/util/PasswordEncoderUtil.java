package com.theblood.identityservice.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtil {

    public static String encode(String password) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        return encoder.encode(password);
    }
}