package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * MessageReaction - Emoji reactions
 */
@Entity
@Table(name = "message_reaction")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReaction extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "reaction_id", length = 50, nullable = false)
    private String reactionId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Size(max = 50)
    @Column(name = "emoji", length = 50, nullable = false)
    private String emoji;

    @Size(max = 20)
    @Column(name = "emoji_display", length = 20)
    private String emojiDisplay;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "attachments", "readReceipts", "reactions", "conversation" }, allowSetters = true)
    private Message message;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getReactionId() {
        return this.reactionId;
    }

    public MessageReaction reactionId(String reactionId) {
        this.setReactionId(reactionId);
        return this;
    }

    public void setReactionId(String reactionId) {
        this.reactionId = reactionId;
    }

    public String getUserId() {
        return this.userId;
    }

    public MessageReaction userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public MessageReaction emoji(String emoji) {
        this.setEmoji(emoji);
        return this;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getEmojiDisplay() {
        return this.emojiDisplay;
    }

    public MessageReaction emojiDisplay(String emojiDisplay) {
        this.setEmojiDisplay(emojiDisplay);
        return this;
    }

    public void setEmojiDisplay(String emojiDisplay) {
        this.emojiDisplay = emojiDisplay;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.reactionId;
    }

    @Override
    public void setId(String id) {
        this.reactionId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public MessageReaction setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public MessageReaction message(Message message) {
        this.setMessage(message);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReaction)) {
            return false;
        }
        return getReactionId() != null && getReactionId().equals(((MessageReaction) o).getReactionId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageReaction{" +
            "reactionId=" + getReactionId() +
            ", userId='" + getUserId() + "'" +
            ", emoji='" + getEmoji() + "'" +
            ", emojiDisplay='" + getEmojiDisplay() + "'" +
            "}";
    }
}
