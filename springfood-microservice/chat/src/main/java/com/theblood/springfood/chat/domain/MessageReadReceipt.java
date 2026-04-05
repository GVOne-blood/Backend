package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * MessageReadReceipt - Read tracking
 */
@Entity
@Table(
    name = "message_read_receipt",
    indexes = {
        @Index(name = "idx_receipt_msg_user", columnList = "message_id,user_id", unique = true)
    }
)
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReadReceipt extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "receipt_id", length = 50, nullable = false)
    private String receiptId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    @Size(max = 50)
    @Column(name = "device_type", length = 50)
    private String deviceType;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "attachments", "readReceipts", "reactions", "conversation" }, allowSetters = true)
    private Message message;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getReceiptId() {
        return this.receiptId;
    }

    public MessageReadReceipt receiptId(String receiptId) {
        this.setReceiptId(receiptId);
        return this;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getUserId() {
        return this.userId;
    }

    public MessageReadReceipt userId(String userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getReadAt() {
        return this.readAt;
    }

    public MessageReadReceipt readAt(Instant readAt) {
        this.setReadAt(readAt);
        return this;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public MessageReadReceipt deviceType(String deviceType) {
        this.setDeviceType(deviceType);
        return this;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.receiptId;
    }

    @Override
    public void setId(String id) {
        this.receiptId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public MessageReadReceipt setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public MessageReadReceipt message(Message message) {
        this.setMessage(message);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReadReceipt)) {
            return false;
        }
        return getReceiptId() != null && getReceiptId().equals(((MessageReadReceipt) o).getReceiptId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageReadReceipt{" +
            "receiptId=" + getReceiptId() +
            ", userId='" + getUserId() + "'" +
            ", readAt='" + getReadAt() + "'" +
            ", deviceType='" + getDeviceType() + "'" +
            "}";
    }
}
