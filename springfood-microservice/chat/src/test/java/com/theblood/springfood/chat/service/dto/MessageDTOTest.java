package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageDTO.class);
        MessageDTO messageDTO1 = new MessageDTO();
        messageDTO1.setMessageId("id1");
        MessageDTO messageDTO2 = new MessageDTO();
        assertThat(messageDTO1).isNotEqualTo(messageDTO2);
        messageDTO2.setMessageId(messageDTO1.getMessageId());
        assertThat(messageDTO1).isEqualTo(messageDTO2);
        messageDTO2.setMessageId("id2");
        assertThat(messageDTO1).isNotEqualTo(messageDTO2);
        messageDTO1.setMessageId(null);
        assertThat(messageDTO1).isNotEqualTo(messageDTO2);
    }
}
