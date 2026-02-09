package com.theblood.springfood.chat.domain.enumeration;

public enum EventType {
    CHAT,       // Tin nhắn chat bình thường
    JOIN,       // User join conversation (online)
    LEAVE,      // User leave conversation
    TYPING,     // User đang gõ...
    READ        // Đánh dấu đã đọc
}
