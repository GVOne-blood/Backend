package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.ConversationTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageAttachmentTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageReactionTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageReadReceiptTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Message.class);
        Message message1 = getMessageSample1();
        Message message2 = new Message();
        assertThat(message1).isNotEqualTo(message2);

        message2.setMessageId(message1.getMessageId());
        assertThat(message1).isEqualTo(message2);

        message2 = getMessageSample2();
        assertThat(message1).isNotEqualTo(message2);
    }

    @Test
    void attachmentsTest() {
        Message message = getMessageRandomSampleGenerator();
        MessageAttachment messageAttachmentBack = getMessageAttachmentRandomSampleGenerator();

        message.addAttachments(messageAttachmentBack);
        assertThat(message.getAttachments()).containsOnly(messageAttachmentBack);
        assertThat(messageAttachmentBack.getMessage()).isEqualTo(message);

        message.removeAttachments(messageAttachmentBack);
        assertThat(message.getAttachments()).doesNotContain(messageAttachmentBack);
        assertThat(messageAttachmentBack.getMessage()).isNull();

        message.attachments(new HashSet<>(Set.of(messageAttachmentBack)));
        assertThat(message.getAttachments()).containsOnly(messageAttachmentBack);
        assertThat(messageAttachmentBack.getMessage()).isEqualTo(message);

        message.setAttachments(new HashSet<>());
        assertThat(message.getAttachments()).doesNotContain(messageAttachmentBack);
        assertThat(messageAttachmentBack.getMessage()).isNull();
    }

    @Test
    void readReceiptsTest() {
        Message message = getMessageRandomSampleGenerator();
        MessageReadReceipt messageReadReceiptBack = getMessageReadReceiptRandomSampleGenerator();

        message.addReadReceipts(messageReadReceiptBack);
        assertThat(message.getReadReceipts()).containsOnly(messageReadReceiptBack);
        assertThat(messageReadReceiptBack.getMessage()).isEqualTo(message);

        message.removeReadReceipts(messageReadReceiptBack);
        assertThat(message.getReadReceipts()).doesNotContain(messageReadReceiptBack);
        assertThat(messageReadReceiptBack.getMessage()).isNull();

        message.readReceipts(new HashSet<>(Set.of(messageReadReceiptBack)));
        assertThat(message.getReadReceipts()).containsOnly(messageReadReceiptBack);
        assertThat(messageReadReceiptBack.getMessage()).isEqualTo(message);

        message.setReadReceipts(new HashSet<>());
        assertThat(message.getReadReceipts()).doesNotContain(messageReadReceiptBack);
        assertThat(messageReadReceiptBack.getMessage()).isNull();
    }

    @Test
    void reactionsTest() {
        Message message = getMessageRandomSampleGenerator();
        MessageReaction messageReactionBack = getMessageReactionRandomSampleGenerator();

        message.addReactions(messageReactionBack);
        assertThat(message.getReactions()).containsOnly(messageReactionBack);
        assertThat(messageReactionBack.getMessage()).isEqualTo(message);

        message.removeReactions(messageReactionBack);
        assertThat(message.getReactions()).doesNotContain(messageReactionBack);
        assertThat(messageReactionBack.getMessage()).isNull();

        message.reactions(new HashSet<>(Set.of(messageReactionBack)));
        assertThat(message.getReactions()).containsOnly(messageReactionBack);
        assertThat(messageReactionBack.getMessage()).isEqualTo(message);

        message.setReactions(new HashSet<>());
        assertThat(message.getReactions()).doesNotContain(messageReactionBack);
        assertThat(messageReactionBack.getMessage()).isNull();
    }

    @Test
    void conversationTest() {
        Message message = getMessageRandomSampleGenerator();
        Conversation conversationBack = getConversationRandomSampleGenerator();

        message.setConversation(conversationBack);
        assertThat(message.getConversation()).isEqualTo(conversationBack);

        message.conversation(null);
        assertThat(message.getConversation()).isNull();
    }
}
