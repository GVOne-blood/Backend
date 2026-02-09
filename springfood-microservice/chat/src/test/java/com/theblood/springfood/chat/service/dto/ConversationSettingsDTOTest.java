package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConversationSettingsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConversationSettingsDTO.class);
        ConversationSettingsDTO conversationSettingsDTO1 = new ConversationSettingsDTO();
        conversationSettingsDTO1.setSettingsId("id1");
        ConversationSettingsDTO conversationSettingsDTO2 = new ConversationSettingsDTO();
        assertThat(conversationSettingsDTO1).isNotEqualTo(conversationSettingsDTO2);
        conversationSettingsDTO2.setSettingsId(conversationSettingsDTO1.getSettingsId());
        assertThat(conversationSettingsDTO1).isEqualTo(conversationSettingsDTO2);
        conversationSettingsDTO2.setSettingsId("id2");
        assertThat(conversationSettingsDTO1).isNotEqualTo(conversationSettingsDTO2);
        conversationSettingsDTO1.setSettingsId(null);
        assertThat(conversationSettingsDTO1).isNotEqualTo(conversationSettingsDTO2);
    }
}
