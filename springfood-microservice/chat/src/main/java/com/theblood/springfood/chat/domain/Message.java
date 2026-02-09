package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * Message - Individual chat messages
 */
@Entity
@Table(name = "message")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Message extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "message_id", length = 50, nullable = false)
    private String messageId;

    @Size(max = 100)
    @Column(name = "client_message_id", length = 100)
    private String clientMessageId;

    @NotNull
    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Size(max = 100)
    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Size(max = 500)
    @Column(name = "sender_avatar", length = 500)
    private String senderAvatar;

    /**
     * Type: TEXT, IMAGE, VIDEO, FILE, AUDIO, LOCATION, STICKER, SYSTEM, ORDER_CARD, PRODUCT_CARD
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "message_type", length = 30, nullable = false)
    private String messageType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Size(max = 200)
    @Column(name = "content_preview", length = 200)
    private String contentPreview;

    @Column(name = "reply_to_message_id")
    private String replyToMessageId;

    @Size(max = 200)
    @Column(name = "reply_to_preview", length = 200)
    private String replyToPreview;

    @Column(name = "forwarded_from_message_id")
    private String forwardedFromMessageId;

    @Column(name = "forwarded_from_conversation_id")
    private String forwardedFromConversationId;

    @Size(max = 50)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Size(max = 100)
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /**
     * Status: SENDING, SENT, DELIVERED, READ, FAILED
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "is_edited")
    private Integer isEdited;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "is_deleted")
    private Integer isDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Size(max = 100)
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @Column(name = "reaction_count")
    private Integer reactionCount;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "message")
    @JsonIgnoreProperties(value = { "message" }, allowSetters = true)
    private Set<MessageAttachment> attachments = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "message")
    @JsonIgnoreProperties(value = { "message" }, allowSetters = true)
    private Set<MessageReadReceipt> readReceipts = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "message")
    @JsonIgnoreProperties(value = { "message" }, allowSetters = true)
    private Set<MessageReaction> reactions = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "settings", "participants", "messages" }, allowSetters = true)
    private Conversation conversation;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getMessageId() {
        return this.messageId;
    }

    public Message messageId(String messageId) {
        this.setMessageId(messageId);
        return this;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getClientMessageId() {
        return this.clientMessageId;
    }

    public Message clientMessageId(String clientMessageId) {
        this.setClientMessageId(clientMessageId);
        return this;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public String getSenderId() {
        return this.senderId;
    }

    public Message senderId(String senderId) {
        this.setSenderId(senderId);
        return this;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public Message senderName(String senderName) {
        this.setSenderName(senderName);
        return this;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return this.senderAvatar;
    }

    public Message senderAvatar(String senderAvatar) {
        this.setSenderAvatar(senderAvatar);
        return this;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public Message messageType(String messageType) {
        this.setMessageType(messageType);
        return this;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return this.content;
    }

    public Message content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentPreview() {
        return this.contentPreview;
    }

    public Message contentPreview(String contentPreview) {
        this.setContentPreview(contentPreview);
        return this;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getReplyToMessageId() {
        return this.replyToMessageId;
    }

    public Message replyToMessageId(String replyToMessageId) {
        this.setReplyToMessageId(replyToMessageId);
        return this;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getReplyToPreview() {
        return this.replyToPreview;
    }

    public Message replyToPreview(String replyToPreview) {
        this.setReplyToPreview(replyToPreview);
        return this;
    }

    public void setReplyToPreview(String replyToPreview) {
        this.replyToPreview = replyToPreview;
    }

    public String getForwardedFromMessageId() {
        return this.forwardedFromMessageId;
    }

    public Message forwardedFromMessageId(String forwardedFromMessageId) {
        this.setForwardedFromMessageId(forwardedFromMessageId);
        return this;
    }

    public void setForwardedFromMessageId(String forwardedFromMessageId) {
        this.forwardedFromMessageId = forwardedFromMessageId;
    }

    public String getForwardedFromConversationId() {
        return this.forwardedFromConversationId;
    }

    public Message forwardedFromConversationId(String forwardedFromConversationId) {
        this.setForwardedFromConversationId(forwardedFromConversationId);
        return this;
    }

    public void setForwardedFromConversationId(String forwardedFromConversationId) {
        this.forwardedFromConversationId = forwardedFromConversationId;
    }

    public String getReferenceType() {
        return this.referenceType;
    }

    public Message referenceType(String referenceType) {
        this.setReferenceType(referenceType);
        return this;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return this.referenceId;
    }

    public Message referenceId(String referenceId) {
        this.setReferenceId(referenceId);
        return this;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getStatus() {
        return this.status;
    }

    public Message status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsEdited() {
        return this.isEdited;
    }

    public Message isEdited(Integer isEdited) {
        this.setIsEdited(isEdited);
        return this;
    }

    public void setIsEdited(Integer isEdited) {
        this.isEdited = isEdited;
    }

    public Instant getEditedAt() {
        return this.editedAt;
    }

    public Message editedAt(Instant editedAt) {
        this.setEditedAt(editedAt);
        return this;
    }

    public void setEditedAt(Instant editedAt) {
        this.editedAt = editedAt;
    }

    public Integer getIsDeleted() {
        return this.isDeleted;
    }

    public Message isDeleted(Integer isDeleted) {
        this.setIsDeleted(isDeleted);
        return this;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Instant getDeletedAt() {
        return this.deletedAt;
    }

    public Message deletedAt(Instant deletedAt) {
        this.setDeletedAt(deletedAt);
        return this;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return this.deletedBy;
    }

    public Message deletedBy(String deletedBy) {
        this.setDeletedBy(deletedBy);
        return this;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Integer getReactionCount() {
        return this.reactionCount;
    }

    public Message reactionCount(Integer reactionCount) {
        this.setReactionCount(reactionCount);
        return this;
    }

    public void setReactionCount(Integer reactionCount) {
        this.reactionCount = reactionCount;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.messageId;
    }

    @Override
    public void setId(String id) {
        this.messageId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Message setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Set<MessageAttachment> getAttachments() {
        return this.attachments;
    }

    public void setAttachments(Set<MessageAttachment> messageAttachments) {
        if (this.attachments != null) {
            this.attachments.forEach(i -> i.setMessage(null));
        }
        if (messageAttachments != null) {
            messageAttachments.forEach(i -> i.setMessage(this));
        }
        this.attachments = messageAttachments;
    }

    public Message attachments(Set<MessageAttachment> messageAttachments) {
        this.setAttachments(messageAttachments);
        return this;
    }

    public Message addAttachments(MessageAttachment messageAttachment) {
        this.attachments.add(messageAttachment);
        messageAttachment.setMessage(this);
        return this;
    }

    public Message removeAttachments(MessageAttachment messageAttachment) {
        this.attachments.remove(messageAttachment);
        messageAttachment.setMessage(null);
        return this;
    }

    public Set<MessageReadReceipt> getReadReceipts() {
        return this.readReceipts;
    }

    public void setReadReceipts(Set<MessageReadReceipt> messageReadReceipts) {
        if (this.readReceipts != null) {
            this.readReceipts.forEach(i -> i.setMessage(null));
        }
        if (messageReadReceipts != null) {
            messageReadReceipts.forEach(i -> i.setMessage(this));
        }
        this.readReceipts = messageReadReceipts;
    }

    public Message readReceipts(Set<MessageReadReceipt> messageReadReceipts) {
        this.setReadReceipts(messageReadReceipts);
        return this;
    }

    public Message addReadReceipts(MessageReadReceipt messageReadReceipt) {
        this.readReceipts.add(messageReadReceipt);
        messageReadReceipt.setMessage(this);
        return this;
    }

    public Message removeReadReceipts(MessageReadReceipt messageReadReceipt) {
        this.readReceipts.remove(messageReadReceipt);
        messageReadReceipt.setMessage(null);
        return this;
    }

    public Set<MessageReaction> getReactions() {
        return this.reactions;
    }

    public void setReactions(Set<MessageReaction> messageReactions) {
        if (this.reactions != null) {
            this.reactions.forEach(i -> i.setMessage(null));
        }
        if (messageReactions != null) {
            messageReactions.forEach(i -> i.setMessage(this));
        }
        this.reactions = messageReactions;
    }

    public Message reactions(Set<MessageReaction> messageReactions) {
        this.setReactions(messageReactions);
        return this;
    }

    public Message addReactions(MessageReaction messageReaction) {
        this.reactions.add(messageReaction);
        messageReaction.setMessage(this);
        return this;
    }

    public Message removeReactions(MessageReaction messageReaction) {
        this.reactions.remove(messageReaction);
        messageReaction.setMessage(null);
        return this;
    }

    public Conversation getConversation() {
        return this.conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Message conversation(Conversation conversation) {
        this.setConversation(conversation);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        return getMessageId() != null && getMessageId().equals(((Message) o).getMessageId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Message{" +
            "messageId=" + getMessageId() +
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
            "}";
    }
}
