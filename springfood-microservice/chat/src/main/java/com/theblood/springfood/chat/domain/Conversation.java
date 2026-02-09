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
 * Conversation - Chat room/thread
 */
@Entity
@Table(name = "conversation")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Conversation extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "conversation_id", length = 50, nullable = false)
    private String conversationId;

    /**
     * Type: DIRECT, GROUP, ORDER_SUPPORT, SHOP_SUPPORT
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "conversation_type", length = 30, nullable = false)
    private String conversationType;

    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Size(max = 50)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Size(max = 100)
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Size(max = 200)
    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Size(max = 100)
    @Column(name = "last_message_sender_id", length = 100)
    private String lastMessageSenderId;

    @Column(name = "message_count")
    private Long messageCount;

    @Column(name = "is_archived")
    private Integer isArchived;

    @Column(name = "is_pinned")
    private Integer isPinned;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @JsonIgnoreProperties(value = { "conversation" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private ConversationSettings settings;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "conversation")
    @JsonIgnoreProperties(value = { "conversation" }, allowSetters = true)
    private Set<ConversationParticipant> participants = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "conversation")
    @JsonIgnoreProperties(value = { "attachments", "readReceipts", "reactions", "conversation" }, allowSetters = true)
    private Set<Message> messages = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getConversationId() {
        return this.conversationId;
    }

    public Conversation conversationId(String conversationId) {
        this.setConversationId(conversationId);
        return this;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationType() {
        return this.conversationType;
    }

    public Conversation conversationType(String conversationType) {
        this.setConversationType(conversationType);
        return this;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getName() {
        return this.name;
    }

    public Conversation name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public Conversation description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public Conversation avatarUrl(String avatarUrl) {
        this.setAvatarUrl(avatarUrl);
        return this;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getReferenceType() {
        return this.referenceType;
    }

    public Conversation referenceType(String referenceType) {
        this.setReferenceType(referenceType);
        return this;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return this.referenceId;
    }

    public Conversation referenceId(String referenceId) {
        this.setReferenceId(referenceId);
        return this;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getLastMessagePreview() {
        return this.lastMessagePreview;
    }

    public Conversation lastMessagePreview(String lastMessagePreview) {
        this.setLastMessagePreview(lastMessagePreview);
        return this;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public Instant getLastMessageAt() {
        return this.lastMessageAt;
    }

    public Conversation lastMessageAt(Instant lastMessageAt) {
        this.setLastMessageAt(lastMessageAt);
        return this;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastMessageSenderId() {
        return this.lastMessageSenderId;
    }

    public Conversation lastMessageSenderId(String lastMessageSenderId) {
        this.setLastMessageSenderId(lastMessageSenderId);
        return this;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public Long getMessageCount() {
        return this.messageCount;
    }

    public Conversation messageCount(Long messageCount) {
        this.setMessageCount(messageCount);
        return this;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getIsArchived() {
        return this.isArchived;
    }

    public Conversation isArchived(Integer isArchived) {
        this.setIsArchived(isArchived);
        return this;
    }

    public void setIsArchived(Integer isArchived) {
        this.isArchived = isArchived;
    }

    public Integer getIsPinned() {
        return this.isPinned;
    }

    public Conversation isPinned(Integer isPinned) {
        this.setIsPinned(isPinned);
        return this;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.conversationId;
    }

    @Override
    public void setId(String id) {
        this.conversationId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Conversation setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public ConversationSettings getSettings() {
        return this.settings;
    }

    public void setSettings(ConversationSettings conversationSettings) {
        this.settings = conversationSettings;
    }

    public Conversation settings(ConversationSettings conversationSettings) {
        this.setSettings(conversationSettings);
        return this;
    }

    public Set<ConversationParticipant> getParticipants() {
        return this.participants;
    }

    public void setParticipants(Set<ConversationParticipant> conversationParticipants) {
        if (this.participants != null) {
            this.participants.forEach(i -> i.setConversation(null));
        }
        if (conversationParticipants != null) {
            conversationParticipants.forEach(i -> i.setConversation(this));
        }
        this.participants = conversationParticipants;
    }

    public Conversation participants(Set<ConversationParticipant> conversationParticipants) {
        this.setParticipants(conversationParticipants);
        return this;
    }

    public Conversation addParticipants(ConversationParticipant conversationParticipant) {
        this.participants.add(conversationParticipant);
        conversationParticipant.setConversation(this);
        return this;
    }

    public Conversation removeParticipants(ConversationParticipant conversationParticipant) {
        this.participants.remove(conversationParticipant);
        conversationParticipant.setConversation(null);
        return this;
    }

    public Set<Message> getMessages() {
        return this.messages;
    }

    public void setMessages(Set<Message> messages) {
        if (this.messages != null) {
            this.messages.forEach(i -> i.setConversation(null));
        }
        if (messages != null) {
            messages.forEach(i -> i.setConversation(this));
        }
        this.messages = messages;
    }

    public Conversation messages(Set<Message> messages) {
        this.setMessages(messages);
        return this;
    }

    public Conversation addMessages(Message message) {
        this.messages.add(message);
        message.setConversation(this);
        return this;
    }

    public Conversation removeMessages(Message message) {
        this.messages.remove(message);
        message.setConversation(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conversation)) {
            return false;
        }
        return getConversationId() != null && getConversationId().equals(((Conversation) o).getConversationId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Conversation{" +
            "conversationId=" + getConversationId() +
            ", conversationType='" + getConversationType() + "'" +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", referenceType='" + getReferenceType() + "'" +
            ", referenceId='" + getReferenceId() + "'" +
            ", lastMessagePreview='" + getLastMessagePreview() + "'" +
            ", lastMessageAt='" + getLastMessageAt() + "'" +
            ", lastMessageSenderId='" + getLastMessageSenderId() + "'" +
            ", messageCount=" + getMessageCount() +
            ", isArchived=" + getIsArchived() +
            ", isPinned=" + getIsPinned() +
            "}";
    }
}
