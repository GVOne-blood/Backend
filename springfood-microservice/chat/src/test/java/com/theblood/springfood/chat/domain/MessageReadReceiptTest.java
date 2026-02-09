package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.MessageReadReceiptTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageReadReceiptTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageReadReceipt.class);
        MessageReadReceipt messageReadReceipt1 = getMessageReadReceiptSample1();
        MessageReadReceipt messageReadReceipt2 = new MessageReadReceipt();
        assertThat(messageReadReceipt1).isNotEqualTo(messageReadReceipt2);

        messageReadReceipt2.setReceiptId(messageReadReceipt1.getReceiptId());
        assertThat(messageReadReceipt1).isEqualTo(messageReadReceipt2);

        messageReadReceipt2 = getMessageReadReceiptSample2();
        assertThat(messageReadReceipt1).isNotEqualTo(messageReadReceipt2);
    }

    @Test
    void messageTest() {
        MessageReadReceipt messageReadReceipt = getMessageReadReceiptRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        messageReadReceipt.setMessage(messageBack);
        assertThat(messageReadReceipt.getMessage()).isEqualTo(messageBack);

        messageReadReceipt.message(null);
        assertThat(messageReadReceipt.getMessage()).isNull();
    }
}
