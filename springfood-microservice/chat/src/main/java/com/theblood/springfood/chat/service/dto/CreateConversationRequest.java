package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for creating a new conversation.
 */
@Data
@NoArgsConstructor
@Schema(description = "Request to create a new conversation")
public class CreateConversationRequest implements Serializable {

    @NotNull
    @Size(max = 30)
    @Schema(description = "Conversation type: DIRECT, ORDER_SUPPORT, SHOP_SUPPORT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String conversationType;

    @Size(max = 100)
    @Schema(description = "Conversation name (optional for DIRECT)")
    private String name;

    @Size(max = 500)
    @Schema(description = "Conversation description")
    private String description;

    @Size(max = 500)
    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @NotNull
    @Schema(description = "List of participant user IDs", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> participantIds;

    @Size(max = 50)
    @Schema(description = "Reference type: ORDER, SHOP")
    private String referenceType;

    @Size(max = 100)
    @Schema(description = "Reference ID for linked entities")
    private String referenceId;
}
