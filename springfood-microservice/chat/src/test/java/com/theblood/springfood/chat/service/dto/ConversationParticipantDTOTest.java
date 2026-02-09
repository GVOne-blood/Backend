package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConversationParticipantDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConversationParticipantDTO.class);
        ConversationParticipantDTO conversationParticipantDTO1 = new ConversationParticipantDTO();
        conversationParticipantDTO1.setParticipantId("id1");
        ConversationParticipantDTO conversationParticipantDTO2 = new ConversationParticipantDTO();
        assertThat(conversationParticipantDTO1).isNotEqualTo(conversationParticipantDTO2);
        conversationParticipantDTO2.setParticipantId(conversationParticipantDTO1.getParticipantId());
        assertThat(conversationParticipantDTO1).isEqualTo(conversationParticipantDTO2);
        conversationParticipantDTO2.setParticipantId("id2");
        assertThat(conversationParticipantDTO1).isNotEqualTo(conversationParticipantDTO2);
        conversationParticipantDTO1.setParticipantId(null);
        assertThat(conversationParticipantDTO1).isNotEqualTo(conversationParticipantDTO2);
    }
}
