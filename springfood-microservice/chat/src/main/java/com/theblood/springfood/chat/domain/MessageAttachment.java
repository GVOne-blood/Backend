package com.theblood.springfood.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

/**
 * MessageAttachment - Media/files attached to messages
 */
@Entity
@Table(name = "message_attachment")
@JsonIgnoreProperties(value = { "new", "id" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageAttachment extends AbstractAuditingEntity<String> implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    @Id
    @UuidGenerator
    @Column(name = "attachment_id", length = 50, nullable = false)
    private String attachmentId;

    @NotNull
    @Column(name = "media_id", nullable = false)
    private String mediaId;

    /**
     * Type: IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "attachment_type", length = 20, nullable = false)
    private String attachmentType;

    @Size(max = 255)
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Size(max = 1000)
    @Column(name = "url", length = 1000)
    private String url;

    @Size(max = 1000)
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "display_order")
    private Integer displayOrder;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "attachments", "readReceipts", "reactions", "conversation" }, allowSetters = true)
    private Message message;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getAttachmentId() {
        return this.attachmentId;
    }

    public MessageAttachment attachmentId(String attachmentId) {
        this.setAttachmentId(attachmentId);
        return this;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getMediaId() {
        return this.mediaId;
    }

    public MessageAttachment mediaId(String mediaId) {
        this.setMediaId(mediaId);
        return this;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getAttachmentType() {
        return this.attachmentType;
    }

    public MessageAttachment attachmentType(String attachmentType) {
        this.setAttachmentType(attachmentType);
        return this;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getFileName() {
        return this.fileName;
    }

    public MessageAttachment fileName(String fileName) {
        this.setFileName(fileName);
        return this;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public MessageAttachment fileSize(Long fileSize) {
        this.setFileSize(fileSize);
        return this;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public MessageAttachment mimeType(String mimeType) {
        this.setMimeType(mimeType);
        return this;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUrl() {
        return this.url;
    }

    public MessageAttachment url(String url) {
        this.setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public MessageAttachment thumbnailUrl(String thumbnailUrl) {
        this.setThumbnailUrl(thumbnailUrl);
        return this;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getWidth() {
        return this.width;
    }

    public MessageAttachment width(Integer width) {
        this.setWidth(width);
        return this;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return this.height;
    }

    public MessageAttachment height(Integer height) {
        this.setHeight(height);
        return this;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public MessageAttachment duration(Integer duration) {
        this.setDuration(duration);
        return this;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDisplayOrder() {
        return this.displayOrder;
    }

    public MessageAttachment displayOrder(Integer displayOrder) {
        this.setDisplayOrder(displayOrder);
        return this;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    @Override
    public String getId() {
        return this.attachmentId;
    }

    @Override
    public void setId(String id) {
        this.attachmentId = id;
    }

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public MessageAttachment setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public MessageAttachment message(Message message) {
        this.setMessage(message);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageAttachment)) {
            return false;
        }
        return getAttachmentId() != null && getAttachmentId().equals(((MessageAttachment) o).getAttachmentId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageAttachment{" +
            "attachmentId=" + getAttachmentId() +
            ", mediaId='" + getMediaId() + "'" +
            ", attachmentType='" + getAttachmentType() + "'" +
            ", fileName='" + getFileName() + "'" +
            ", fileSize=" + getFileSize() +
            ", mimeType='" + getMimeType() + "'" +
            ", url='" + getUrl() + "'" +
            ", thumbnailUrl='" + getThumbnailUrl() + "'" +
            ", width=" + getWidth() +
            ", height=" + getHeight() +
            ", duration=" + getDuration() +
            ", displayOrder=" + getDisplayOrder() +
            "}";
    }
}
