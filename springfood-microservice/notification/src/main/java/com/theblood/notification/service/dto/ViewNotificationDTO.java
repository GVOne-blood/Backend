package com.theblood.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ViewNotificationDTO {
    String actionUrl;
    String message;
}
