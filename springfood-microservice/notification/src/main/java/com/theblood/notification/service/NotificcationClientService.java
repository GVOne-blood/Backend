package com.theblood.notification.service;

import com.theblood.notification.service.dto.NotificationDTO;
import com.theblood.springfood.client.api.NotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificcationClientService {

    private final NotificationClient notificationClient;

    public void handleCreationNotification(List<NotificationClient.NotificationsDTO> notificationsDTOList) {
        List<NotificationDTO> reqDto = new ArrayList<>();


    }

}
