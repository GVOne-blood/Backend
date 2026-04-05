package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for sending a message via WebSocket.
 */
@Data
@NoArgsConstructor
@Schema(description = "Request to send a message in a conversation")
public class SendMessageRequest implements Serializable {

    @NotNull
    @Schema(description = "Conversation ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String conversationId;

    @NotNull
    @Schema(description = "Message content", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Size(max = 100)
    @Schema(description = "Client-generated message ID for deduplication")
    private String clientMessageId;

    @Size(max = 30)
    @Schema(description = "Message type: TEXT, SYSTEM, ORDER_CARD, PRODUCT_CARD")
    private String messageType = "TEXT"; // Default to TEXT

    @Schema(description = "ID of message being replied to")
    private String replyToMessageId;

    @Size(max = 50)
    @Schema(description = "Reference type: PRODUCT, ORDER")
    private String referenceType;

    @Size(max = 100)
    @Schema(description = "Reference ID for linked entities")
    private String referenceId;

    @Schema(description = "Additional metadata as JSON string")
    private String metadata;
}
