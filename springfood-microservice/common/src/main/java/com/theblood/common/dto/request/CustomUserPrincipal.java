package com.theblood.common.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * this is an object to set SecurityContextHolder for downstream services
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserPrincipal implements Serializable {
    private UUID userId;
    private String username;

    // Convenience method
    public String getUserIdString() {
        return userId != null ? userId.toString() : null;
    }
}
