package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.MessageReport} entity.
 */
@Schema(description = "MessageReport - Report messages")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReportDTO implements Serializable {

    @NotNull
    private String reportId;

    @NotNull
    private String reporterId;

    @NotNull
    private String messageId;

    @NotNull
    @Size(max = 30)
    @Schema(description = "Reason: SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, SCAM, OTHER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Size(max = 1000)
    private String details;

    @NotNull
    @Size(max = 20)
    @Schema(description = "Status: PENDING, REVIEWED, RESOLVED, DISMISSED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Size(max = 100)
    private String reviewedBy;

    private Instant reviewedAt;

    @Size(max = 1000)
    private String reviewNotes;

    @Size(max = 500)
    private String actionTaken;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReportDTO)) {
            return false;
        }

        MessageReportDTO messageReportDTO = (MessageReportDTO) o;
        if (this.reportId == null) {
            return false;
        }
        return Objects.equals(this.reportId, messageReportDTO.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reportId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageReportDTO{" +
            "reportId='" + getReportId() + "'" +
            ", reporterId='" + getReporterId() + "'" +
            ", messageId='" + getMessageId() + "'" +
            ", reason='" + getReason() + "'" +
            ", details='" + getDetails() + "'" +
            ", status='" + getStatus() + "'" +
            ", reviewedBy='" + getReviewedBy() + "'" +
            ", reviewedAt='" + getReviewedAt() + "'" +
            ", reviewNotes='" + getReviewNotes() + "'" +
            ", actionTaken='" + getActionTaken() + "'" +
            "}";
    }
}
