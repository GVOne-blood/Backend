package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * AI Message - Lưu lịch sử chat với AI Assistant
 * 
 * Bảng này lưu bản sao của messages từ SPRING_AI_CHAT_MEMORY
 * để phục vụ business logic (hiển thị UI, search, analytics)
 */
@Entity
@Table(
    name = "ai_message",
    indexes = {
        @Index(name = "idx_ai_msg_conv_created", columnList = "conversation_id,created_date"),
        @Index(name = "idx_ai_msg_user", columnList = "user_id,created_date"),
        @Index(name = "idx_ai_msg_type", columnList = "message_type")
    }
)
@Getter
@Setter
@ToString
@JsonIgnoreProperties(value = { "new", "id" })
public class AIMessage extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "message_id", length = 50, nullable = false)
    private String messageId;

    /**
     * Conversation ID - khớp với conversationId trong SPRING_AI_CHAT_MEMORY
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "conversation_id", length = 100, nullable = false)
    private String conversationId;

    /**
     * User ID - người sở hữu conversation
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    /**
     * Message Type: USER, ASSISTANT, SYSTEM
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "message_type", length = 20, nullable = false)
    private String messageType;

    /**
     * Nội dung message
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * Preview ngắn gọn của content (200 chars)
     */
    @Size(max = 200)
    @Column(name = "content_preview", length = 200)
    private String contentPreview;

    /**
     * Metadata bổ sung (JSON format)
     * Ví dụ: model info, token count, latency, etc.
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Token count (nếu có)
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    /**
     * Response time (ms) - chỉ cho ASSISTANT messages
     */
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    /**
     * Model name (gemini-1.5-flash, gpt-4, etc.)
     */
    @Size(max = 50)
    @Column(name = "model_name", length = 50)
    private String modelName;

    /**
     * Soft delete flag
     */
    @Column(name = "is_deleted")
    private Integer isDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    
    // Fluent setters for builder pattern
    public AIMessage messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public AIMessage conversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public AIMessage userId(String userId) {
        this.userId = userId;
        return this;
    }

    public AIMessage messageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public AIMessage content(String content) {
        this.content = content;
        return this;
    }

    public AIMessage contentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
        return this;
    }

    public AIMessage metadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public AIMessage tokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
        return this;
    }

    public AIMessage responseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
        return this;
    }

    public AIMessage modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public AIMessage isDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public AIMessage deletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
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

    public AIMessage setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AIMessage)) {
            return false;
        }
        return messageId != null && messageId.equals(((AIMessage) o).messageId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
