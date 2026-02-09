package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UserPresenceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserPresenceDTO.class);
        UserPresenceDTO userPresenceDTO1 = new UserPresenceDTO();
        userPresenceDTO1.setPresenceId("id1");
        UserPresenceDTO userPresenceDTO2 = new UserPresenceDTO();
        assertThat(userPresenceDTO1).isNotEqualTo(userPresenceDTO2);
        userPresenceDTO2.setPresenceId(userPresenceDTO1.getPresenceId());
        assertThat(userPresenceDTO1).isEqualTo(userPresenceDTO2);
        userPresenceDTO2.setPresenceId("id2");
        assertThat(userPresenceDTO1).isNotEqualTo(userPresenceDTO2);
        userPresenceDTO1.setPresenceId(null);
        assertThat(userPresenceDTO1).isNotEqualTo(userPresenceDTO2);
    }
}
