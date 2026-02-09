package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * UserPresence - Online status tracking
 */
@Entity
@Table(name = "user_presence")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserPresence extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "presence_id", length = 50, nullable = false)
    private String presenceId;

    @NotNull
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    /**
     * Status: ONLINE, AWAY, BUSY, OFFLINE
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Size(max = 100)
    @Column(name = "status_message", length = 100)
    private String statusMessage;

    @NotNull
    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "active_conversation_id")
    private String activeConversationId;

    @Size(max = 50)
    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Size(max = 100)
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Size(max = 100)
    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getPresenceId() {
        return this.presenceId;
    }

    public UserPresence presenceId(String presenceId) {
        this.setPresenceId(presenceId);
        return this;
    }

    public void setPresenceId(String presenceId) {
        this.presenceId = presenceId;
    }

    public String getUserId() {
        return this.userId;
    }

    public UserPresence userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return this.status;
    }

    public UserPresence status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public UserPresence statusMessage(String statusMessage) {
        this.setStatusMessage(statusMessage);
        return this;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Instant getLastSeenAt() {
        return this.lastSeenAt;
    }

    public UserPresence lastSeenAt(Instant lastSeenAt) {
        this.setLastSeenAt(lastSeenAt);
        return this;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getActiveConversationId() {
        return this.activeConversationId;
    }

    public UserPresence activeConversationId(String activeConversationId) {
        this.setActiveConversationId(activeConversationId);
        return this;
    }

    public void setActiveConversationId(String activeConversationId) {
        this.activeConversationId = activeConversationId;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public UserPresence deviceType(String deviceType) {
        this.setDeviceType(deviceType);
        return this;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public UserPresence deviceId(String deviceId) {
        this.setDeviceId(deviceId);
        return this;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public UserPresence sessionId(String sessionId) {
        this.setSessionId(sessionId);
        return this;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getLastActivityAt() {
        return this.lastActivityAt;
    }

    public UserPresence lastActivityAt(Instant lastActivityAt) {
        this.setLastActivityAt(lastActivityAt);
        return this;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.presenceId;
    }

    @Override
    public void setId(String id) {
        this.presenceId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public UserPresence setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPresence)) {
            return false;
        }
        return getPresenceId() != null && getPresenceId().equals(((UserPresence) o).getPresenceId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserPresence{" +
            "presenceId=" + getPresenceId() +
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
