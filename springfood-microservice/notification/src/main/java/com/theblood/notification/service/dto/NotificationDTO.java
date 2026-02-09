package com.theblood.notification.service.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO implements Serializable {

    @Size(max = 50)
    private String notificationId;

    @Size(max = 50)
    private String tableName;

    @Size(max = 50)
    private String objectId;

    @NotNull
    @Size(max = 255)
    private String notificationType;

    @Size(max = 50)
    private String eventId;

    @Size(max = 50)
    private String receiveId;

    @NotNull
    @Min(value = 0)
    @Max(value = 1)
    private Integer isActive;

    @NotNull
    @Size(min = 20, max = 2000)
    private String title;

    @Size(max = 2000)
    private String body;

    private String actionUrl;

    @NotNull
    private boolean viewed;

    @NotNull
    private boolean clicked;

    private Instant createdDate;

    private String createdBy;

    private Instant lastModifiedDate;

    private String lastModifiedBy;


}
