package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.MessageReadReceipt} entity.
 */
@Schema(description = "MessageReadReceipt - Read tracking")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReadReceiptDTO implements Serializable {

    @NotNull
    private String receiptId;

    @NotNull
    private String userId;

    @NotNull
    private Instant readAt;

    @Size(max = 50)
    private String deviceType;

    @NotNull
    private MessageDTO message;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public MessageDTO getMessage() {
        return message;
    }

    public void setMessage(MessageDTO message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReadReceiptDTO)) {
            return false;
        }

        MessageReadReceiptDTO messageReadReceiptDTO = (MessageReadReceiptDTO) o;
        if (this.receiptId == null) {
            return false;
        }
        return Objects.equals(this.receiptId, messageReadReceiptDTO.receiptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.receiptId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageReadReceiptDTO{" +
            "receiptId='" + getReceiptId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", readAt='" + getReadAt() + "'" +
            ", deviceType='" + getDeviceType() + "'" +
            ", message=" + getMessage() +
            "}";
    }
}
