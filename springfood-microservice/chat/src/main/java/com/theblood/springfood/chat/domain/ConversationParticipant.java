package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * ConversationParticipant - Users in conversations
 */
@Entity
@Table(name = "conversation_participant")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationParticipant extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "participant_id", length = 50, nullable = false)
    private String participantId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Size(max = 100)
    @Column(name = "display_name", length = 100)
    private String displayName;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * Role: OWNER, ADMIN, MEMBER
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "role", length = 20, nullable = false)
    private String role;

    /**
     * Status: ACTIVE, LEFT, REMOVED, MUTED
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Size(max = 50)
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "last_read_message_id")
    private String lastReadMessageId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "is_muted")
    private Integer isMuted;

    @Column(name = "mute_until")
    private Instant muteUntil;

    @Column(name = "is_pinned")
    private Integer isPinned;

    @Column(name = "pinned_at")
    private Instant pinnedAt;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Size(max = 100)
    @Column(name = "added_by", length = 100)
    private String addedBy;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "settings", "participants", "messages" }, allowSetters = true)
    private Conversation conversation;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getParticipantId() {
        return this.participantId;
    }

    public ConversationParticipant participantId(String participantId) {
        this.setParticipantId(participantId);
        return this;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getUserId() {
        return this.userId;
    }

    public ConversationParticipant userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public ConversationParticipant displayName(String displayName) {
        this.setDisplayName(displayName);
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public ConversationParticipant avatarUrl(String avatarUrl) {
        this.setAvatarUrl(avatarUrl);
        return this;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return this.role;
    }

    public ConversationParticipant role(String role) {
        this.setRole(role);
        return this;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return this.status;
    }

    public ConversationParticipant status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNickname() {
        return this.nickname;
    }

    public ConversationParticipant nickname(String nickname) {
        this.setNickname(nickname);
        return this;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLastReadMessageId() {
        return this.lastReadMessageId;
    }

    public ConversationParticipant lastReadMessageId(String lastReadMessageId) {
        this.setLastReadMessageId(lastReadMessageId);
        return this;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public Instant getLastReadAt() {
        return this.lastReadAt;
    }

    public ConversationParticipant lastReadAt(Instant lastReadAt) {
        this.setLastReadAt(lastReadAt);
        return this;
    }

    public void setLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public Integer getUnreadCount() {
        return this.unreadCount;
    }

    public ConversationParticipant unreadCount(Integer unreadCount) {
        this.setUnreadCount(unreadCount);
        return this;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getIsMuted() {
        return this.isMuted;
    }

    public ConversationParticipant isMuted(Integer isMuted) {
        this.setIsMuted(isMuted);
        return this;
    }

    public void setIsMuted(Integer isMuted) {
        this.isMuted = isMuted;
    }

    public Instant getMuteUntil() {
        return this.muteUntil;
    }

    public ConversationParticipant muteUntil(Instant muteUntil) {
        this.setMuteUntil(muteUntil);
        return this;
    }

    public void setMuteUntil(Instant muteUntil) {
        this.muteUntil = muteUntil;
    }

    public Integer getIsPinned() {
        return this.isPinned;
    }

    public ConversationParticipant isPinned(Integer isPinned) {
        this.setIsPinned(isPinned);
        return this;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Instant getPinnedAt() {
        return this.pinnedAt;
    }

    public ConversationParticipant pinnedAt(Instant pinnedAt) {
        this.setPinnedAt(pinnedAt);
        return this;
    }

    public void setPinnedAt(Instant pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    public Instant getJoinedAt() {
        return this.joinedAt;
    }

    public ConversationParticipant joinedAt(Instant joinedAt) {
        this.setJoinedAt(joinedAt);
        return this;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLeftAt() {
        return this.leftAt;
    }

    public ConversationParticipant leftAt(Instant leftAt) {
        this.setLeftAt(leftAt);
        return this;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public String getAddedBy() {
        return this.addedBy;
    }

    public ConversationParticipant addedBy(String addedBy) {
        this.setAddedBy(addedBy);
        return this;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.participantId;
    }

    @Override
    public void setId(String id) {
        this.participantId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public ConversationParticipant setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Conversation getConversation() {
        return this.conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public ConversationParticipant conversation(Conversation conversation) {
        this.setConversation(conversation);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationParticipant)) {
            return false;
        }
        return getParticipantId() != null && getParticipantId().equals(((ConversationParticipant) o).getParticipantId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConversationParticipant{" +
            "participantId=" + getParticipantId() +
            ", userId='" + getUserId() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            ", avatarUrl='" + getAvatarUrl() + "'" +
            ", role='" + getRole() + "'" +
            ", status='" + getStatus() + "'" +
            ", nickname='" + getNickname() + "'" +
            ", lastReadMessageId='" + getLastReadMessageId() + "'" +
            ", lastReadAt='" + getLastReadAt() + "'" +
            ", unreadCount=" + getUnreadCount() +
            ", isMuted=" + getIsMuted() +
            ", muteUntil='" + getMuteUntil() + "'" +
            ", isPinned=" + getIsPinned() +
            ", pinnedAt='" + getPinnedAt() + "'" +
            ", joinedAt='" + getJoinedAt() + "'" +
            ", leftAt='" + getLeftAt() + "'" +
            ", addedBy='" + getAddedBy() + "'" +
            "}";
    }
}
