package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * MessageReport - Report messages
 */
@Entity
@Table(name = "message_report")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReport extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "report_id", length = 50, nullable = false)
    private String reportId;

    @NotNull
    @Column(name = "reporter_id", nullable = false)
    private String reporterId;

    @NotNull
    @Column(name = "message_id", nullable = false)
    private String messageId;

    /**
     * Reason: SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, SCAM, OTHER
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "reason", length = 30, nullable = false)
    private String reason;

    @Size(max = 1000)
    @Column(name = "details", length = 1000)
    private String details;

    /**
     * Status: PENDING, REVIEWED, RESOLVED, DISMISSED
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Size(max = 100)
    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Size(max = 1000)
    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Size(max = 500)
    @Column(name = "action_taken", length = 500)
    private String actionTaken;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getReportId() {
        return this.reportId;
    }

    public MessageReport reportId(String reportId) {
        this.setReportId(reportId);
        return this;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterId() {
        return this.reporterId;
    }

    public MessageReport reporterId(String reporterId) {
        this.setReporterId(reporterId);
        return this;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public MessageReport messageId(String messageId) {
        this.setMessageId(messageId);
        return this;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReason() {
        return this.reason;
    }

    public MessageReport reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return this.details;
    }

    public MessageReport details(String details) {
        this.setDetails(details);
        return this;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return this.status;
    }

    public MessageReport status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewedBy() {
        return this.reviewedBy;
    }

    public MessageReport reviewedBy(String reviewedBy) {
        this.setReviewedBy(reviewedBy);
        return this;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return this.reviewedAt;
    }

    public MessageReport reviewedAt(Instant reviewedAt) {
        this.setReviewedAt(reviewedAt);
        return this;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return this.reviewNotes;
    }

    public MessageReport reviewNotes(String reviewNotes) {
        this.setReviewNotes(reviewNotes);
        return this;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getActionTaken() {
        return this.actionTaken;
    }

    public MessageReport actionTaken(String actionTaken) {
        this.setActionTaken(actionTaken);
        return this;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.reportId;
    }

    @Override
    public void setId(String id) {
        this.reportId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public MessageReport setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReport)) {
            return false;
        }
        return getReportId() != null && getReportId().equals(((MessageReport) o).getReportId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageReport{" +
            "reportId=" + getReportId() +
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
