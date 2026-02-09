package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageAttachmentDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageAttachmentDTO.class);
        MessageAttachmentDTO messageAttachmentDTO1 = new MessageAttachmentDTO();
        messageAttachmentDTO1.setAttachmentId("id1");
        MessageAttachmentDTO messageAttachmentDTO2 = new MessageAttachmentDTO();
        assertThat(messageAttachmentDTO1).isNotEqualTo(messageAttachmentDTO2);
        messageAttachmentDTO2.setAttachmentId(messageAttachmentDTO1.getAttachmentId());
        assertThat(messageAttachmentDTO1).isEqualTo(messageAttachmentDTO2);
        messageAttachmentDTO2.setAttachmentId("id2");
        assertThat(messageAttachmentDTO1).isNotEqualTo(messageAttachmentDTO2);
        messageAttachmentDTO1.setAttachmentId(null);
        assertThat(messageAttachmentDTO1).isNotEqualTo(messageAttachmentDTO2);
    }
}
