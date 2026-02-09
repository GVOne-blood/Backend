package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.MessageAttachmentTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageAttachmentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageAttachment.class);
        MessageAttachment messageAttachment1 = getMessageAttachmentSample1();
        MessageAttachment messageAttachment2 = new MessageAttachment();
        assertThat(messageAttachment1).isNotEqualTo(messageAttachment2);

        messageAttachment2.setAttachmentId(messageAttachment1.getAttachmentId());
        assertThat(messageAttachment1).isEqualTo(messageAttachment2);

        messageAttachment2 = getMessageAttachmentSample2();
        assertThat(messageAttachment1).isNotEqualTo(messageAttachment2);
    }

    @Test
    void messageTest() {
        MessageAttachment messageAttachment = getMessageAttachmentRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        messageAttachment.setMessage(messageBack);
        assertThat(messageAttachment.getMessage()).isEqualTo(messageBack);

        messageAttachment.message(null);
        assertThat(messageAttachment.getMessage()).isNull();
    }
}
