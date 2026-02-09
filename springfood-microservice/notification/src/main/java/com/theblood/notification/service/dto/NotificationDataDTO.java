package com.theblood.notification.service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDataDTO {

    String accountId;
    String deviceId;
    String pushToken;
    String notificationTitle;
    String notificationType;
    String eventId;
    Instant createdDate;


}
