package com.theblood.springfood.chat.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.theblood.springfood.chat.domain.MessageAttachment} entity.
 */
@Schema(description = "MessageAttachment - Media/files attached to messages")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageAttachmentDTO implements Serializable {

    @NotNull
    private String attachmentId;

    @NotNull
    private String mediaId;

    @NotNull
    @Size(max = 20)
    @Schema(description = "Type: IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attachmentType;

    @Size(max = 255)
    private String fileName;

    private Long fileSize;

    @Size(max = 100)
    private String mimeType;

    @Size(max = 1000)
    private String url;

    @Size(max = 1000)
    private String thumbnailUrl;

    private Integer width;

    private Integer height;

    private Integer duration;

    private Integer displayOrder;

    @NotNull
    private MessageDTO message;

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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
        if (!(o instanceof MessageAttachmentDTO)) {
            return false;
        }

        MessageAttachmentDTO messageAttachmentDTO = (MessageAttachmentDTO) o;
        if (this.attachmentId == null) {
            return false;
        }
        return Objects.equals(this.attachmentId, messageAttachmentDTO.attachmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.attachmentId);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageAttachmentDTO{" +
            "attachmentId='" + getAttachmentId() + "'" +
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
            ", message=" + getMessage() +
            "}";
    }
}
