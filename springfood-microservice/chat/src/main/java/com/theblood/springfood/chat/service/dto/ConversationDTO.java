package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.Conversation} entity.
 */
@Schema(description = "Conversation - Chat room/thread")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationDTO implements Serializable {

    @NotNull
    private String conversationId;

    @NotNull
    @Size(max = 30)
    @Schema(description = "Type: DIRECT, GROUP, ORDER_SUPPORT, SHOP_SUPPORT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String conversationType;

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 50)
    private String referenceType;

    @Size(max = 100)
    private String referenceId;

    @Size(max = 200)
    private String lastMessagePreview;

    private Instant lastMessageAt;

    @Size(max = 100)
    private String lastMessageSenderId;

    private String lastMessageId;

    private Long messageCount;

    private Integer unreadCount;

    private Integer isArchived;

    private Integer isPinned;

    private Instant createdAt;

    private ConversationSettingsDTO settings;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Integer isArchived) {
        this.isArchived = isArchived;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ConversationSettingsDTO getSettings() {
        return settings;
    }

    public void setSettings(ConversationSettingsDTO settings) {
        this.settings = settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationDTO)) {
            return false;
        }

        ConversationDTO conversationDTO = (ConversationDTO) o;
        if (this.conversationId == null) {
            return false;
        }
        return Objects.equals(this.conversationId, conversationDTO.conversationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.conversationId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConversationDTO{" +
            "conversationId='" + getConversationId() + "'" +
            ", conversationType='" + getConversationType() + "'" +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", referenceType='" + getReferenceType() + "'" +
            ", referenceId='" + getReferenceId() + "'" +
            ", lastMessagePreview='" + getLastMessagePreview() + "'" +
            ", lastMessageAt='" + getLastMessageAt() + "'" +
            ", lastMessageSenderId='" + getLastMessageSenderId() + "'" +
            ", lastMessageId='" + getLastMessageId() + "'" +
            ", messageCount=" + getMessageCount() +
            ", unreadCount=" + getUnreadCount() +
            ", isArchived=" + getIsArchived() +
            ", isPinned=" + getIsPinned() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", settings=" + getSettings() +
            "}";
    }
}
