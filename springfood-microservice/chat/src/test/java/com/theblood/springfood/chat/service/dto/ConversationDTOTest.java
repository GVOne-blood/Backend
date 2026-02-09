package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConversationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConversationDTO.class);
        ConversationDTO conversationDTO1 = new ConversationDTO();
        conversationDTO1.setConversationId("id1");
        ConversationDTO conversationDTO2 = new ConversationDTO();
        assertThat(conversationDTO1).isNotEqualTo(conversationDTO2);
        conversationDTO2.setConversationId(conversationDTO1.getConversationId());
        assertThat(conversationDTO1).isEqualTo(conversationDTO2);
        conversationDTO2.setConversationId("id2");
        assertThat(conversationDTO1).isNotEqualTo(conversationDTO2);
        conversationDTO1.setConversationId(null);
        assertThat(conversationDTO1).isNotEqualTo(conversationDTO2);
    }
}
