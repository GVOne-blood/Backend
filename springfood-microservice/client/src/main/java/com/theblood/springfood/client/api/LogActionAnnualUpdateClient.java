package com.theblood.springfood.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@ServiceClient(value = "log-actions", path = "/api/log-action-annual-updates")
public interface LogActionAnnualUpdateClient extends BaseClient {

    @ClientMethod(
            httpMethod = "POST",
            path = "",
            grpcMethod = "createLogActionAnnualUpdate",
            idempotent = true
    )
    ClientResponse<Boolean> createLogActionAnnualUpdate(ClientRequest<LogActionAnnualUpdateClient.LogActionAnnualUpdateDto> data);

    @ClientMethod(
            httpMethod = "POST",
            path = "/search",
            grpcMethod = "findByTableNameAndObjectId",
            idempotent = true
    )
    ClientResponse<java.util.List<LogActionAnnualUpdateClient.LogActionAnnualUpdateDto>> findByTableNameAndObjectId(
            ClientRequest<LogActionAnnualUpdateClient.LogActionSearchRequest> data
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class LogActionAnnualUpdateDto {
        private String id;
        private String accountId;
        private String userName;
        private String shopId;
        private String actionType;
        private String oldValue;
        private String newValue;
        private String description;
        private String ipAddress;
        private String userAgent;
        private String tableName;
        private String objectId;
        private java.time.Instant createdDate;
        private String createdBy;
        private java.time.Instant lastModifiedDate;
        private String lastModifiedBy;
        private Integer affectCurrent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class LogActionSearchRequest {
        private String tableName;
        private String objectId;
        private String sort;
    }
}
