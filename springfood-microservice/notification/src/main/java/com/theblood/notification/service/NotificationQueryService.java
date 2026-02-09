package com.theblood.notification.service;

import com.theblood.common.exception.custom.CustomException;
import com.theblood.common.exception.custom.StatusCode;
import com.theblood.notification.domain.Notification;
import com.theblood.notification.service.dto.NotificationDataDTO;
import com.theblood.springfood.client.api.AuthenticationClient;
import com.theblood.springfood.client.api.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final AuthenticationClient authenticationClient;

    public void sendNotificationToUserDevices(Notification notifications) {

        String receiveId = notifications.getReceiveId();
        AuthenticationClient.NotificationUserDevicesDTO devices = validateUserDeveices(receiveId);

        NotificationDataDTO message = NotificationDataDTO.builder()
            .deviceId(devices.getDeviceId())
            .accountId(devices.getAccountId())
            .pushToken(devices.getPushToken())
            .notificationTitle(notifications.getTitle())
            .notificationType(notifications.getNotificationType())
            .createdDate(notifications.getCreatedDate())
            .eventId(notifications.getEventId())
            .build();

        // push message to kafka topic
        //kafkaTemplate.send("notification-topic", message);
    }

    private AuthenticationClient.NotificationUserDevicesDTO validateUserDeveices(String receiveId) {

        ClientResponse res = authenticationClient.getNotificationUserDevices(receiveId);
        if (res.getBody() == null || !res.isSuccess())
            throw new CustomException(StatusCode.BAD_REQUEST.getCode(), "User devices not found");

        return (AuthenticationClient.NotificationUserDevicesDTO) res.getBody();
    }


}
