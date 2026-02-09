package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.MessageReactionTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageReactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageReaction.class);
        MessageReaction messageReaction1 = getMessageReactionSample1();
        MessageReaction messageReaction2 = new MessageReaction();
        assertThat(messageReaction1).isNotEqualTo(messageReaction2);

        messageReaction2.setReactionId(messageReaction1.getReactionId());
        assertThat(messageReaction1).isEqualTo(messageReaction2);

        messageReaction2 = getMessageReactionSample2();
        assertThat(messageReaction1).isNotEqualTo(messageReaction2);
    }

    @Test
    void messageTest() {
        MessageReaction messageReaction = getMessageReactionRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        messageReaction.setMessage(messageBack);
        assertThat(messageReaction.getMessage()).isEqualTo(messageBack);

        messageReaction.message(null);
        assertThat(messageReaction.getMessage()).isNull();
    }
}
