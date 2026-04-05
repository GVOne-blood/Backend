package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.MessageReaction} entity.
 */
@Schema(description = "MessageReaction - Emoji reactions")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReactionDTO implements Serializable {

    @NotNull
    private String reactionId;

    @NotNull
    private String userId;

    @NotNull
    @Size(max = 50)
    private String emoji;

    @Size(max = 20)
    private String emojiDisplay;

    @NotNull
    private String messageId;

    public String getReactionId() {
        return reactionId;
    }

    public void setReactionId(String reactionId) {
        this.reactionId = reactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getEmojiDisplay() {
        return emojiDisplay;
    }

    public void setEmojiDisplay(String emojiDisplay) {
        this.emojiDisplay = emojiDisplay;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReactionDTO)) {
            return false;
        }

        MessageReactionDTO messageReactionDTO = (MessageReactionDTO) o;
        if (this.reactionId == null) {
            return false;
        }
        return Objects.equals(this.reactionId, messageReactionDTO.reactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reactionId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageReactionDTO{" +
            "reactionId='" + getReactionId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", emoji='" + getEmoji() + "'" +
            ", emojiDisplay='" + getEmojiDisplay() + "'" +
            ", messageId='" + getMessageId() + "'" +
            "}";
    }
}
