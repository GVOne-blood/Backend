package com.theblood.notification.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Notification getNotificationSample1() {
        return new Notification()
            .notificationId("notificationId1")
            .tableName("tableName1")
            .objectId("objectId1")
            .notificationType("notificationType1")
            .eventId("eventId1")
            .receiveId("receiveId1")
            .isActive(1)
            .title("title1")
            .body("body1")
            .actionUrl("actionUrl1")
            .isViewed(1)
            .isClicked(1)
            .lastModifiedBy("lastModifiedBy1")
            .createdBy("createdBy1");
    }

    public static Notification getNotificationSample2() {
        return new Notification()
            .notificationId("notificationId2")
            .tableName("tableName2")
            .objectId("objectId2")
            .notificationType("notificationType2")
            .eventId("eventId2")
            .receiveId("receiveId2")
            .isActive(2)
            .title("title2")
            .body("body2")
            .actionUrl("actionUrl2")
            .isViewed(2)
            .isClicked(2)
            .lastModifiedBy("lastModifiedBy2")
            .createdBy("createdBy2");
    }

    public static Notification getNotificationRandomSampleGenerator() {
        return new Notification()
            .notificationId(UUID.randomUUID().toString())
            .tableName(UUID.randomUUID().toString())
            .objectId(UUID.randomUUID().toString())
            .notificationType(UUID.randomUUID().toString())
            .eventId(UUID.randomUUID().toString())
            .receiveId(UUID.randomUUID().toString())
            .isActive(intCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .body(UUID.randomUUID().toString())
            .actionUrl(UUID.randomUUID().toString())
            .isViewed(intCount.incrementAndGet())
            .isClicked(intCount.incrementAndGet())
            .lastModifiedBy(UUID.randomUUID().toString())
            .createdBy(UUID.randomUUID().toString());
    }
}
