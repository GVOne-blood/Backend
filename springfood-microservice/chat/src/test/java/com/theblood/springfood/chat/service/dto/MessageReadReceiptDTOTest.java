package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageReadReceiptDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageReadReceiptDTO.class);
        MessageReadReceiptDTO messageReadReceiptDTO1 = new MessageReadReceiptDTO();
        messageReadReceiptDTO1.setReceiptId("id1");
        MessageReadReceiptDTO messageReadReceiptDTO2 = new MessageReadReceiptDTO();
        assertThat(messageReadReceiptDTO1).isNotEqualTo(messageReadReceiptDTO2);
        messageReadReceiptDTO2.setReceiptId(messageReadReceiptDTO1.getReceiptId());
        assertThat(messageReadReceiptDTO1).isEqualTo(messageReadReceiptDTO2);
        messageReadReceiptDTO2.setReceiptId("id2");
        assertThat(messageReadReceiptDTO1).isNotEqualTo(messageReadReceiptDTO2);
        messageReadReceiptDTO1.setReceiptId(null);
        assertThat(messageReadReceiptDTO1).isNotEqualTo(messageReadReceiptDTO2);
    }
}
