package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for typing indicator via WebSocket.
 */
@Data
@NoArgsConstructor
@Schema(description = "Request to indicate user is typing")
public class TypingRequest implements Serializable {

    @NotNull
    @Schema(description = "Conversation ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String conversationId;

    @NotNull
    @Schema(description = "Is user typing (true) or stopped typing (false)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isTyping;
}
