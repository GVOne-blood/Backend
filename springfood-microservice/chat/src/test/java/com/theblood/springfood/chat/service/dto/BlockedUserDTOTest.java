package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BlockedUserDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BlockedUserDTO.class);
        BlockedUserDTO blockedUserDTO1 = new BlockedUserDTO();
        blockedUserDTO1.setBlockId("id1");
        BlockedUserDTO blockedUserDTO2 = new BlockedUserDTO();
        assertThat(blockedUserDTO1).isNotEqualTo(blockedUserDTO2);
        blockedUserDTO2.setBlockId(blockedUserDTO1.getBlockId());
        assertThat(blockedUserDTO1).isEqualTo(blockedUserDTO2);
        blockedUserDTO2.setBlockId("id2");
        assertThat(blockedUserDTO1).isNotEqualTo(blockedUserDTO2);
        blockedUserDTO1.setBlockId(null);
        assertThat(blockedUserDTO1).isNotEqualTo(blockedUserDTO2);
    }
}
