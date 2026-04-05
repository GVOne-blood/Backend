package com.theblood.springfood.chat.service.dto;

import java.io.Serializable;

/**
 * DTO for unread message count response.
 */
public class UnreadCountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String conversationId;
    private Integer unreadCount;

    public UnreadCountResponse() {
        // Empty constructor needed for Jackson
    }

    public UnreadCountResponse(String conversationId, Integer unreadCount) {
        this.conversationId = conversationId;
        this.unreadCount = unreadCount;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public String toString() {
        return "UnreadCountResponse{" +
            "conversationId='" + conversationId + '\'' +
            ", unreadCount=" + unreadCount +
            '}';
    }
}
