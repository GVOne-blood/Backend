package com.theblood.springfood.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ServiceClient(value = "notification", path = "/api/notifications")
public interface NotificationClient extends BaseClient {
    @ClientMethod
            (
                    httpMethod = "POST",
                    path = "/list",
                    grpcMethod = "SendNotification",
                    idempotent = true
            )
    ClientResponse<List<NotificationsDTO>> sendNotification(ClientRequest<List<NotificationsDTO>> request);

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    class NotificationsDTO {
        private String notificationId;
        private String tableName;
        private String objectId;
        private String notificationType;
        private String eventId;
        private String receiveId;
        private Integer isActive;
        private String title;
        private String body;
        private String actionUrl;
        private boolean viewed;
        private boolean clicked;
        private Instant createdDate;

    }
}
