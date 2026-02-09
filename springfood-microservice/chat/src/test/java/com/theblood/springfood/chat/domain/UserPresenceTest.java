package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.UserPresenceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UserPresenceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserPresence.class);
        UserPresence userPresence1 = getUserPresenceSample1();
        UserPresence userPresence2 = new UserPresence();
        assertThat(userPresence1).isNotEqualTo(userPresence2);

        userPresence2.setPresenceId(userPresence1.getPresenceId());
        assertThat(userPresence1).isEqualTo(userPresence2);

        userPresence2 = getUserPresenceSample2();
        assertThat(userPresence1).isNotEqualTo(userPresence2);
    }
}
