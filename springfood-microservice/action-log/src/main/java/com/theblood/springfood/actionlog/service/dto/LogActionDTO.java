package com.theblood.springfood.actionlog.service.dto;

import com.theblood.springfood.actionlog.domain.LogAction;
import com.theblood.springfood.actionlog.domain.enumeration.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A DTO for the {@link LogAction} entity.
 */
@Schema(description = "Action Log entity\nLưu nhật ký hệ thống")
@SuppressWarnings("common-java:DuplicatedBlocks")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogActionDTO implements Serializable {

    private String id;

    private String accountId;

    @Size(max = 50)
    private String userName;

    private String organizationId;

    @NotNull
    private ActionType actionType;

    @Lob
    private String oldValue;

    @Lob
    private String newValue;

    @Lob
    private String description;

    @Size(max = 50)
    private String ipAddress;

    @Lob
    private String userAgent;

    @Lob
    private String tableName;

    @Size(max = 50)
    private String objectId;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogActionDTO)) {
            return false;
        }

        LogActionDTO logActionDTO = (LogActionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, logActionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LogActionDTO{" +
                "id='" + getId() + "'" +
                ", accountId=" + getAccountId() +
                ", userName='" + getUserName() + "'" +
                ", organizationId=" + getOrganizationId() +
                ", actionType='" + getActionType() + "'" +
                ", oldValue='" + getOldValue() + "'" +
                ", newValue='" + getNewValue() + "'" +
                ", description='" + getDescription() + "'" +
                ", ipAddress='" + getIpAddress() + "'" +
                ", userAgent='" + getUserAgent() + "'" +
                ", tableName='" + getTableName() + "'" +
                ", objectId='" + getObjectId() + "'" +
                ", createdBy='" + getCreatedBy() + "'" +
                ", createdDate='" + getCreatedDate() + "'" +
                ", lastModifiedBy='" + getLastModifiedBy() + "'" +
                ", lastModifiedDate='" + getLastModifiedDate() + "'" +
                "}";
    }
}
