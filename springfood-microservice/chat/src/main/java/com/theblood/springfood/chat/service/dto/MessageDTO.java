package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.Message} entity.
 */
@Schema(description = "Message - Individual chat messages")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageDTO implements Serializable {

    @NotNull
    private String messageId;

    @Size(max = 100)
    private String clientMessageId;

    @NotNull
    private String senderId;

    @Size(max = 100)
    private String senderName;

    @Size(max = 500)
    private String senderAvatar;

    @NotNull
    @Size(max = 30)
    @Schema(
        description = "Type: TEXT, IMAGE, VIDEO, FILE, AUDIO, LOCATION, STICKER, SYSTEM, ORDER_CARD, PRODUCT_CARD",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String messageType;

    private String content;

    @Size(max = 200)
    private String contentPreview;

    private String replyToMessageId;

    @Size(max = 200)
    private String replyToPreview;

    private String forwardedFromMessageId;

    private String forwardedFromConversationId;

    @Size(max = 50)
    private String referenceType;

    @Size(max = 100)
    private String referenceId;

    @NotNull
    @Size(max = 20)
    @Schema(description = "Status: SENDING, SENT, DELIVERED, READ, FAILED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    private Integer isEdited;

    private Instant editedAt;

    private Integer isDeleted;

    private Instant deletedAt;

    @Size(max = 100)
    private String deletedBy;

    private Integer reactionCount;

    private Instant createdAt;

    @NotNull
    private String conversationId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getReplyToPreview() {
        return replyToPreview;
    }

    public void setReplyToPreview(String replyToPreview) {
        this.replyToPreview = replyToPreview;
    }

    public String getForwardedFromMessageId() {
        return forwardedFromMessageId;
    }

    public void setForwardedFromMessageId(String forwardedFromMessageId) {
        this.forwardedFromMessageId = forwardedFromMessageId;
    }

    public String getForwardedFromConversationId() {
        return forwardedFromConversationId;
    }

    public void setForwardedFromConversationId(String forwardedFromConversationId) {
        this.forwardedFromConversationId = forwardedFromConversationId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Integer isEdited) {
        this.isEdited = isEdited;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Instant editedAt) {
        this.editedAt = editedAt;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Integer getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(Integer reactionCount) {
        this.reactionCount = reactionCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageDTO)) {
            return false;
        }

        MessageDTO messageDTO = (MessageDTO) o;
        if (this.messageId == null) {
            return false;
        }
        return Objects.equals(this.messageId, messageDTO.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.messageId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageDTO{" +
            "messageId='" + getMessageId() + "'" +
            ", clientMessageId='" + getClientMessageId() + "'" +
            ", senderId='" + getSenderId() + "'" +
            ", senderName='" + getSenderName() + "'" +
            ", senderAvatar='" + getSenderAvatar() + "'" +
            ", messageType='" + getMessageType() + "'" +
            ", content='" + getContent() + "'" +
            ", contentPreview='" + getContentPreview() + "'" +
            ", replyToMessageId='" + getReplyToMessageId() + "'" +
            ", replyToPreview='" + getReplyToPreview() + "'" +
            ", forwardedFromMessageId='" + getForwardedFromMessageId() + "'" +
            ", forwardedFromConversationId='" + getForwardedFromConversationId() + "'" +
            ", referenceType='" + getReferenceType() + "'" +
            ", referenceId='" + getReferenceId() + "'" +
            ", status='" + getStatus() + "'" +
            ", isEdited=" + getIsEdited() +
            ", editedAt='" + getEditedAt() + "'" +
            ", isDeleted=" + getIsDeleted() +
            ", deletedAt='" + getDeletedAt() + "'" +
            ", deletedBy='" + getDeletedBy() + "'" +
            ", reactionCount=" + getReactionCount() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", conversationId='" + getConversationId() + "'" +
            "}";
    }
}
