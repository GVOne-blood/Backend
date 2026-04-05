package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Kafka event DTO for read receipts.
 * Published to "chat-read-receipts" topic for processing read status updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kafka event for read receipt processing")
public class ReadReceiptEvent implements Serializable {

    private String conversationId;
    private String userId;
    private String lastReadMessageId;
    private Instant readAt;
}
