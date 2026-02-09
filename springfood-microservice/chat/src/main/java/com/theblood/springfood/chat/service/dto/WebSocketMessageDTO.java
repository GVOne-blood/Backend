package com.theblood.springfood.chat.service.dto;

import com.theblood.springfood.chat.domain.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessageDTO {
    private String clientMessageId; // Client tự sinh UUID để tracking
    private String conversationId;
    private String content;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String messageType; // "TEXT", "IMAGE", ...
    private EventType eventType; // "CHAT", "JOIN", "TYPING"
    private Instant timestamp;
}

