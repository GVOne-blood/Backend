package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageReactionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageReactionDTO.class);
        MessageReactionDTO messageReactionDTO1 = new MessageReactionDTO();
        messageReactionDTO1.setReactionId("id1");
        MessageReactionDTO messageReactionDTO2 = new MessageReactionDTO();
        assertThat(messageReactionDTO1).isNotEqualTo(messageReactionDTO2);
        messageReactionDTO2.setReactionId(messageReactionDTO1.getReactionId());
        assertThat(messageReactionDTO1).isEqualTo(messageReactionDTO2);
        messageReactionDTO2.setReactionId("id2");
        assertThat(messageReactionDTO1).isNotEqualTo(messageReactionDTO2);
        messageReactionDTO1.setReactionId(null);
        assertThat(messageReactionDTO1).isNotEqualTo(messageReactionDTO2);
    }
}
