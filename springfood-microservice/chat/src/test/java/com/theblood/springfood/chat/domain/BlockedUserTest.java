package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.BlockedUserTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BlockedUserTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BlockedUser.class);
        BlockedUser blockedUser1 = getBlockedUserSample1();
        BlockedUser blockedUser2 = new BlockedUser();
        assertThat(blockedUser1).isNotEqualTo(blockedUser2);

        blockedUser2.setBlockId(blockedUser1.getBlockId());
        assertThat(blockedUser1).isEqualTo(blockedUser2);

        blockedUser2 = getBlockedUserSample2();
        assertThat(blockedUser1).isNotEqualTo(blockedUser2);
    }
}
