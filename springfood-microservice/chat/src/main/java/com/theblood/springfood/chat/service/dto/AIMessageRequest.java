package com.theblood.springfood.chat.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AI chat message request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMessageRequest {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    private String conversationId;
}
