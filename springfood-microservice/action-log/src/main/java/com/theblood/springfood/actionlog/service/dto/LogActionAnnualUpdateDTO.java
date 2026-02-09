package com.theblood.springfood.actionlog.service.dto;

import com.theblood.springfood.actionlog.domain.LogActionAnnualUpdate;
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
 * A DTO for the {@link LogActionAnnualUpdate} entity.
 */
@Schema(description = "Action Log entity\nLưu nhật ký hệ thống")
@SuppressWarnings("common-java:DuplicatedBlocks")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogActionAnnualUpdateDTO implements Serializable {

    private String id;

    private String accountId;

    @Size(max = 50)
    private String userName;

    private String organizationId;

    @NotNull
    private String actionType;

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

    private Integer affectCurrent;

    private Instant createdDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LogActionAnnualUpdateDTO)) {
            return false;
        }

        LogActionAnnualUpdateDTO logActionAnnualUpdateDTO = (LogActionAnnualUpdateDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, logActionAnnualUpdateDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LogActionAnnualUpdateDTO{" +
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
                ", affectCurrent=" + getAffectCurrent() +
                ", createdDate='" + getCreatedDate() + "'" +
                "}";
    }
}
