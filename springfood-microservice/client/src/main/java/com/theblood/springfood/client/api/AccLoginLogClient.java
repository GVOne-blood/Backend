package com.theblood.springfood.client.api;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theblood.common.enums.AuthType;
import com.theblood.common.enums.DeviceType;
import com.theblood.common.enums.LoginEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@BaseClient.ServiceClient(value = "log-actions", path = "/api/acc-login-logs")
public interface AccLoginLogClient extends BaseClient {

    @ClientMethod(
            httpMethod = "POST",
            path = "/create-log",
            grpcMethod = "createAccLoginLog",
            idempotent = true
    )
    ClientResponse<AccLoginLogDto> createAccLoginLog(AccLoginLogDto data);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class AccLoginLogDto {
        private String id;
        private String accountId;
        private AuthType authType;
        private LoginEventType eventType;
        private String eventDetails;
        private String ipAddress;
        private DeviceType deviceType;
        private Instant loginAttemptTime;
    }

}
