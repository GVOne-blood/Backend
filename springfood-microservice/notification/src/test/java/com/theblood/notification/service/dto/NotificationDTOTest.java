package com.theblood.notification.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.notification.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class NotificationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(NotificationDTO.class);
        NotificationDTO notificationDTO1 = new NotificationDTO();
        notificationDTO1.setNotificationId("id1");
        NotificationDTO notificationDTO2 = new NotificationDTO();
        assertThat(notificationDTO1).isNotEqualTo(notificationDTO2);
        notificationDTO2.setNotificationId(notificationDTO1.getNotificationId());
        assertThat(notificationDTO1).isEqualTo(notificationDTO2);
        notificationDTO2.setNotificationId("id2");
        assertThat(notificationDTO1).isNotEqualTo(notificationDTO2);
        notificationDTO1.setNotificationId(null);
        assertThat(notificationDTO1).isNotEqualTo(notificationDTO2);
    }
}
