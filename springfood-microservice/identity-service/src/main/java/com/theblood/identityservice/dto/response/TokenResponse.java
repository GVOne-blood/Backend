package com.theblood.identityservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class TokenResponse {
    String userId;
    String username;
    String accessToken;
    String refreshToken;
    String tokenType = "Bearer";
    Long expiresIn;
}

