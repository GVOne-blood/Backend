package com.theblood.springfood.chat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for AI chat message response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageResponse {

    private String conversationId;
    private String message;
    private String response;
    private Instant timestamp;

    public static AIMessageResponse of(String conversationId, String message, String response) {
        return new AIMessageResponse(conversationId, message, response, Instant.now());
    }
}
