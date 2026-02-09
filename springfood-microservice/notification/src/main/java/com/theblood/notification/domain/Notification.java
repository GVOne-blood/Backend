package com.theblood.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.Instant;

/**
 * A Notification.
 */
@Entity
@Table(name = "notification")
@JsonIgnoreProperties(value = {"new", "id"})
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Notification implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column(name = "notification_id", nullable = false)
    private String notificationId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "object_id")
    private String objectId;

    @NotNull
    @Size(max = 255)
    @Column(name = "notification_type", length = 255, nullable = false)
    private String notificationType;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "receive_id")
    private String receiveId;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Integer isActive;

    @NotNull
    @Size(max = 2000)
    @Column(name = "title", length = 2000, nullable = false)
    private String title;

    @Size(max = 2000)
    @Column(name = "body", length = 2000)
    private String body;

    @Size(max = 200)
    @Column(name = "action_url", length = 200)
    private String actionUrl;

    @NotNull
    @Column(name = "is_viewed", nullable = false)
    private Integer isViewed;

    @NotNull
    @Column(name = "is_clicked", nullable = false)
    private Integer isClicked;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @Size(max = 50)
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public Notification notificationId(String notificationId) {
        this.setNotificationId(notificationId);
        return this;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Notification tableName(String tableName) {
        this.setTableName(tableName);
        return this;
    }

    public String getObjectId() {
        return this.objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Notification objectId(String objectId) {
        this.setObjectId(objectId);
        return this;
    }

    public String getNotificationType() {
        return this.notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Notification notificationType(String notificationType) {
        this.setNotificationType(notificationType);
        return this;
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Notification eventId(String eventId) {
        this.setEventId(eventId);
        return this;
    }

    public String getReceiveId() {
        return this.receiveId;
    }

    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }

    public Notification receiveId(String receiveId) {
        this.setReceiveId(receiveId);
        return this;
    }

    public Integer getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Notification isActive(Integer isActive) {
        this.setIsActive(isActive);
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Notification title(String title) {
        this.setTitle(title);
        return this;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Notification body(String body) {
        this.setBody(body);
        return this;
    }

    public String getActionUrl() {
        return this.actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public Notification actionUrl(String actionUrl) {
        this.setActionUrl(actionUrl);
        return this;
    }

    public Integer getIsViewed() {
        return this.isViewed;
    }

    public void setIsViewed(Integer isViewed) {
        this.isViewed = isViewed;
    }

    public Notification isViewed(Integer isViewed) {
        this.setIsViewed(isViewed);
        return this;
    }

    public Integer getIsClicked() {
        return this.isClicked;
    }

    public void setIsClicked(Integer isClicked) {
        this.isClicked = isClicked;
    }

    public Notification isClicked(Integer isClicked) {
        this.setIsClicked(isClicked);
        return this;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Notification lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Notification lastModifiedBy(String lastModifiedBy) {
        this.setLastModifiedBy(lastModifiedBy);
        return this;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Notification createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Notification createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.notificationId;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Notification setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        return getNotificationId() != null && getNotificationId().equals(((Notification) o).getNotificationId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Notification{" +
            "notificationId=" + getNotificationId() +
            ", tableName='" + getTableName() + "'" +
            ", objectId='" + getObjectId() + "'" +
            ", notificationType='" + getNotificationType() + "'" +
            ", eventId='" + getEventId() + "'" +
            ", receiveId='" + getReceiveId() + "'" +
            ", isActive=" + getIsActive() +
            ", title='" + getTitle() + "'" +
            ", body='" + getBody() + "'" +
            ", actionUrl='" + getActionUrl() + "'" +
            ", isViewed=" + getIsViewed() +
            ", isClicked=" + getIsClicked() +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            ", lastModifiedBy='" + getLastModifiedBy() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            "}";
    }
}
