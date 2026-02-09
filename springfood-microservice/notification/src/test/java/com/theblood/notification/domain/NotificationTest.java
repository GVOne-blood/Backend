package com.theblood.notification.domain;

import static com.theblood.notification.domain.NotificationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.notification.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Notification.class);
        Notification notification1 = getNotificationSample1();
        Notification notification2 = new Notification();
        assertThat(notification1).isNotEqualTo(notification2);

        notification2.setNotificationId(notification1.getNotificationId());
        assertThat(notification1).isEqualTo(notification2);

        notification2 = getNotificationSample2();
        assertThat(notification1).isNotEqualTo(notification2);
    }
}
