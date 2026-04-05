package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * Event DTO for typing indicators.
 * Broadcast via WebSocket to notify participants who is currently typing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Event for typing indicator broadcast")
public class TypingIndicatorEvent implements Serializable {

    private String conversationId;
    private Set<String> typingUserIds;
}
