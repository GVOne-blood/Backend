package com.theblood.common.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * this is an object to set SecurityContextHolder for downstream services
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class CustomUserPrincipal implements Serializable {
    private UUID userId;
    private String username;
    private String shopId;
    private String role;

    // Convenience method
    public String getUserIdString() {
        return userId != null ? userId.toString() : null;
    }
}
