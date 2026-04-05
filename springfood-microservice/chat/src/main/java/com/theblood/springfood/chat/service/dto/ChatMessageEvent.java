package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Kafka event DTO for chat messages.
 * Published to "chat-messages" topic for distribution across service instances.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kafka event for chat message distribution")
public class ChatMessageEvent implements Serializable {

    private String messageId;
    private String clientMessageId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String senderType;
    private String messageType;
    private String content;
    private String metadata;
    private String replyToMessageId;
    private String replyToPreview;
    private String referenceType;
    private String referenceId;
    private String status;
    private Instant createdAt;
}
