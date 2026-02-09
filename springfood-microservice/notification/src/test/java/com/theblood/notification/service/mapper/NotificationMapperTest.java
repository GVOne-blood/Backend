package com.theblood.notification.service.mapper;

import static com.theblood.notification.domain.NotificationAsserts.*;
import static com.theblood.notification.domain.NotificationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationMapperTest {

    private NotificationMapper notificationMapper;

    @BeforeEach
    void setUp() {
        notificationMapper = new NotificationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getNotificationSample1();
        var actual = notificationMapper.toEntity(notificationMapper.toDto(expected));
        assertNotificationAllPropertiesEquals(expected, actual);
    }
}
