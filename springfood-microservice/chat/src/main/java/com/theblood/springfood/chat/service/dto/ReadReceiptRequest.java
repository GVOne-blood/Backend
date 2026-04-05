package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for marking messages as read via WebSocket.
 */
@Data
@NoArgsConstructor
@Schema(description = "Request to mark messages as read")
public class ReadReceiptRequest implements Serializable {

    @NotNull
    @Schema(description = "Conversation ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String conversationId;

    @NotNull
    @Schema(description = "Last read message ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastReadMessageId;
}
