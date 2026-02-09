package com.theblood.springfood.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@ServiceClient(value = "log-actions", path = "/api/log-actions")
public interface LogActionsClient extends BaseClient {

    @ClientMethod(
            httpMethod = "POST",
            path = "",
            grpcMethod = "createLogAction",
            idempotent = true
    )
    ClientResponse<Boolean> createLogAction(ClientRequest<LogActionsClient.LogActionsDto> data);

    @ClientMethod(
            httpMethod = "POST",
            path = "/search",
            grpcMethod = "findByTableNameAndObjectId",
            idempotent = true
    )
    ClientResponse<java.util.List<LogActionsClient.LogActionsDto>> findByTableNameAndObjectId(
            ClientRequest<LogActionsClient.LogActionSearchRequest> data
    );

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class LogActionsDto {
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
