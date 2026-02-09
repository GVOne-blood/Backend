package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.TypingIndicator} entity.
 */
@Schema(description = "TypingIndicator - Who is typing")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TypingIndicatorDTO implements Serializable {

    @NotNull
    private String indicatorId;

    @NotNull
    private String userId;

    @Size(max = 100)
    private String userName;

    @NotNull
    private String conversationId;

    @NotNull
    private Instant startedAt;

    @NotNull
    private Instant expiresAt;

    public String getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypingIndicatorDTO)) {
            return false;
        }

        TypingIndicatorDTO typingIndicatorDTO = (TypingIndicatorDTO) o;
        if (this.indicatorId == null) {
            return false;
        }
        return Objects.equals(this.indicatorId, typingIndicatorDTO.indicatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.indicatorId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TypingIndicatorDTO{" +
            "indicatorId='" + getIndicatorId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", userName='" + getUserName() + "'" +
            ", conversationId='" + getConversationId() + "'" +
            ", startedAt='" + getStartedAt() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            "}";
    }
}
