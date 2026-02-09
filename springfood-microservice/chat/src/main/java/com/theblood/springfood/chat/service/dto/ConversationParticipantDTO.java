package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.ConversationParticipant} entity.
 */
@Schema(description = "ConversationParticipant - Users in conversations")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationParticipantDTO implements Serializable {

    @NotNull
    private String participantId;

    @NotNull
    private String userId;

    @Size(max = 100)
    private String displayName;

    @Size(max = 500)
    private String avatarUrl;

    @NotNull
    @Size(max = 20)
    @Schema(description = "Role: OWNER, ADMIN, MEMBER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @NotNull
    @Size(max = 20)
    @Schema(description = "Status: ACTIVE, LEFT, REMOVED, MUTED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Size(max = 50)
    private String nickname;

    private String lastReadMessageId;

    private Instant lastReadAt;

    private Integer unreadCount;

    private Integer isMuted;

    private Instant muteUntil;

    private Integer isPinned;

    private Instant pinnedAt;

    @NotNull
    private Instant joinedAt;

    private Instant leftAt;

    @Size(max = 100)
    private String addedBy;

    @NotNull
    private ConversationDTO conversation;

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public Instant getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Integer isMuted) {
        this.isMuted = isMuted;
    }

    public Instant getMuteUntil() {
        return muteUntil;
    }

    public void setMuteUntil(Instant muteUntil) {
        this.muteUntil = muteUntil;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Instant getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(Instant pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public ConversationDTO getConversation() {
        return conversation;
    }

    public void setConversation(ConversationDTO conversation) {
        this.conversation = conversation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationParticipantDTO)) {
            return false;
        }

        ConversationParticipantDTO conversationParticipantDTO = (ConversationParticipantDTO) o;
        if (this.participantId == null) {
            return false;
        }
        return Objects.equals(this.participantId, conversationParticipantDTO.participantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.participantId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConversationParticipantDTO{" +
            "participantId='" + getParticipantId() + "'" +
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
            ", conversation=" + getConversation() +
            "}";
    }
}
