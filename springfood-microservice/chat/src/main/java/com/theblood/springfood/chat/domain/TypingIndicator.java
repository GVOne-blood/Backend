package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * TypingIndicator - Who is typing
 */
@Entity
@Table(name = "typing_indicator")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TypingIndicator extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "indicator_id", length = 50, nullable = false)
    private String indicatorId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Size(max = 100)
    @Column(name = "user_name", length = 100)
    private String userName;

    @NotNull
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @NotNull
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getIndicatorId() {
        return this.indicatorId;
    }

    public TypingIndicator indicatorId(String indicatorId) {
        this.setIndicatorId(indicatorId);
        return this;
    }

    public void setIndicatorId(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    public String getUserId() {
        return this.userId;
    }

    public TypingIndicator userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public TypingIndicator userName(String userName) {
        this.setUserName(userName);
        return this;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public TypingIndicator conversationId(String conversationId) {
        this.setConversationId(conversationId);
        return this;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Instant getStartedAt() {
        return this.startedAt;
    }

    public TypingIndicator startedAt(Instant startedAt) {
        this.setStartedAt(startedAt);
        return this;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public TypingIndicator expiresAt(Instant expiresAt) {
        this.setExpiresAt(expiresAt);
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.indicatorId;
    }

    @Override
    public void setId(String id) {
        this.indicatorId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public TypingIndicator setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypingIndicator)) {
            return false;
        }
        return getIndicatorId() != null && getIndicatorId().equals(((TypingIndicator) o).getIndicatorId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TypingIndicator{" +
            "indicatorId=" + getIndicatorId() +
            ", userId='" + getUserId() + "'" +
            ", userName='" + getUserName() + "'" +
            ", conversationId='" + getConversationId() + "'" +
            ", startedAt='" + getStartedAt() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            "}";
    }
}
