package com.theblood.springfood.chat.service.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO for adding a participant to a conversation.
 */
public class AddParticipantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "User ID is required")
    private String userId;

    public AddParticipantRequest() {
        // Empty constructor needed for Jackson
    }

    public AddParticipantRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "AddParticipantRequest{" +
            "userId='" + userId + '\'' +
            '}';
    }
}
