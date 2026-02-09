package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.UserPresence} entity.
 */
@Schema(description = "UserPresence - Online status tracking")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserPresenceDTO implements Serializable {

    @NotNull
    private String presenceId;

    @NotNull
    private String userId;

    @NotNull
    @Size(max = 20)
    @Schema(description = "Status: ONLINE, AWAY, BUSY, OFFLINE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Size(max = 100)
    private String statusMessage;

    @NotNull
    private Instant lastSeenAt;

    private String activeConversationId;

    @Size(max = 50)
    private String deviceType;

    @Size(max = 100)
    private String deviceId;

    @Size(max = 100)
    private String sessionId;

    private Instant lastActivityAt;

    public String getPresenceId() {
        return presenceId;
    }

    public void setPresenceId(String presenceId) {
        this.presenceId = presenceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getActiveConversationId() {
        return activeConversationId;
    }

    public void setActiveConversationId(String activeConversationId) {
        this.activeConversationId = activeConversationId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPresenceDTO)) {
            return false;
        }

        UserPresenceDTO userPresenceDTO = (UserPresenceDTO) o;
        if (this.presenceId == null) {
            return false;
        }
        return Objects.equals(this.presenceId, userPresenceDTO.presenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.presenceId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserPresenceDTO{" +
            "presenceId='" + getPresenceId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", status='" + getStatus() + "'" +
            ", statusMessage='" + getStatusMessage() + "'" +
            ", lastSeenAt='" + getLastSeenAt() + "'" +
            ", activeConversationId='" + getActiveConversationId() + "'" +
            ", deviceType='" + getDeviceType() + "'" +
            ", deviceId='" + getDeviceId() + "'" +
            ", sessionId='" + getSessionId() + "'" +
            ", lastActivityAt='" + getLastActivityAt() + "'" +
            "}";
    }
}
