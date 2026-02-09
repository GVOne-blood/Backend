package com.theblood.springfood.actionlog.service.dto;


import com.theblood.common.enums.AuthType;
import com.theblood.common.enums.DeviceType;
import com.theblood.common.enums.LoginEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("common-java:DuplicatedBlocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccLoginLogDTO implements Serializable {

    @Size(max = 50)
    private String id;

    @Size(max = 50)
    private String accountId;

    @NotNull
    @Size(max = 20)
    private AuthType authType;

    @NotNull
    private LoginEventType eventType;

    @Size(max = 4000)
    private String eventDetails;

    @NotNull
    @Size(max = 45)
    private String ipAddress;

    @Size(max = 20)
    private DeviceType deviceType;

    @NotNull
    private Instant loginAttemptTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccLoginLogDTO)) {
            return false;
        }

        AccLoginLogDTO accLoginLogDTO = (AccLoginLogDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, accLoginLogDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AccLoginLogDTO{" +
                "accAuditLogId='" + getId() + "'" +
                ", accountId='" + getAccountId() + "'" +
                ", authType='" + getAuthType() + "'" +
                ", eventType='" + getEventType() + "'" +
                ", eventDetails='" + getEventDetails() + "'" +
                ", ipAddress='" + getIpAddress() + "'" +
                ", deviceType='" + getDeviceType() + "'" +
                ", loginAttemptTime='" + getLoginAttemptTime() + "'" +
                "}";
    }
}
