package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.ConversationSettings} entity.
 */
@Schema(description = "ConversationSettings - Per-conversation settings")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationSettingsDTO implements Serializable {

    @NotNull
    private String settingsId;

    private Integer onlyAdminCanSend;

    private Integer onlyAdminCanAddMembers;

    private Integer autoDeleteDays;

    private Integer allowReactions;

    private Integer allowReplies;

    private Integer allowAttachments;

    private Integer maxAttachmentSizeMb;

    @Size(max = 500)
    private String allowedFileTypes;

    private Integer showReadReceipts;

    private Integer showTypingIndicators;

    public String getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(String settingsId) {
        this.settingsId = settingsId;
    }

    public Integer getOnlyAdminCanSend() {
        return onlyAdminCanSend;
    }

    public void setOnlyAdminCanSend(Integer onlyAdminCanSend) {
        this.onlyAdminCanSend = onlyAdminCanSend;
    }

    public Integer getOnlyAdminCanAddMembers() {
        return onlyAdminCanAddMembers;
    }

    public void setOnlyAdminCanAddMembers(Integer onlyAdminCanAddMembers) {
        this.onlyAdminCanAddMembers = onlyAdminCanAddMembers;
    }

    public Integer getAutoDeleteDays() {
        return autoDeleteDays;
    }

    public void setAutoDeleteDays(Integer autoDeleteDays) {
        this.autoDeleteDays = autoDeleteDays;
    }

    public Integer getAllowReactions() {
        return allowReactions;
    }

    public void setAllowReactions(Integer allowReactions) {
        this.allowReactions = allowReactions;
    }

    public Integer getAllowReplies() {
        return allowReplies;
    }

    public void setAllowReplies(Integer allowReplies) {
        this.allowReplies = allowReplies;
    }

    public Integer getAllowAttachments() {
        return allowAttachments;
    }

    public void setAllowAttachments(Integer allowAttachments) {
        this.allowAttachments = allowAttachments;
    }

    public Integer getMaxAttachmentSizeMb() {
        return maxAttachmentSizeMb;
    }

    public void setMaxAttachmentSizeMb(Integer maxAttachmentSizeMb) {
        this.maxAttachmentSizeMb = maxAttachmentSizeMb;
    }

    public String getAllowedFileTypes() {
        return allowedFileTypes;
    }

    public void setAllowedFileTypes(String allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }

    public Integer getShowReadReceipts() {
        return showReadReceipts;
    }

    public void setShowReadReceipts(Integer showReadReceipts) {
        this.showReadReceipts = showReadReceipts;
    }

    public Integer getShowTypingIndicators() {
        return showTypingIndicators;
    }

    public void setShowTypingIndicators(Integer showTypingIndicators) {
        this.showTypingIndicators = showTypingIndicators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationSettingsDTO)) {
            return false;
        }

        ConversationSettingsDTO conversationSettingsDTO = (ConversationSettingsDTO) o;
        if (this.settingsId == null) {
            return false;
        }
        return Objects.equals(this.settingsId, conversationSettingsDTO.settingsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.settingsId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConversationSettingsDTO{" +
            "settingsId='" + getSettingsId() + "'" +
            ", onlyAdminCanSend=" + getOnlyAdminCanSend() +
            ", onlyAdminCanAddMembers=" + getOnlyAdminCanAddMembers() +
            ", autoDeleteDays=" + getAutoDeleteDays() +
            ", allowReactions=" + getAllowReactions() +
            ", allowReplies=" + getAllowReplies() +
            ", allowAttachments=" + getAllowAttachments() +
            ", maxAttachmentSizeMb=" + getMaxAttachmentSizeMb() +
            ", allowedFileTypes='" + getAllowedFileTypes() + "'" +
            ", showReadReceipts=" + getShowReadReceipts() +
            ", showTypingIndicators=" + getShowTypingIndicators() +
            "}";
    }
}
