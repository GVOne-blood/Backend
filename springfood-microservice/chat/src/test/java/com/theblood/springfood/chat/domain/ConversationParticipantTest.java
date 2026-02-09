package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.ConversationParticipantTestSamples.*;
import static com.theblood.springfood.chat.domain.ConversationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConversationParticipantTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConversationParticipant.class);
        ConversationParticipant conversationParticipant1 = getConversationParticipantSample1();
        ConversationParticipant conversationParticipant2 = new ConversationParticipant();
        assertThat(conversationParticipant1).isNotEqualTo(conversationParticipant2);

        conversationParticipant2.setParticipantId(conversationParticipant1.getParticipantId());
        assertThat(conversationParticipant1).isEqualTo(conversationParticipant2);

        conversationParticipant2 = getConversationParticipantSample2();
        assertThat(conversationParticipant1).isNotEqualTo(conversationParticipant2);
    }

    @Test
    void conversationTest() {
        ConversationParticipant conversationParticipant = getConversationParticipantRandomSampleGenerator();
        Conversation conversationBack = getConversationRandomSampleGenerator();

        conversationParticipant.setConversation(conversationBack);
        assertThat(conversationParticipant.getConversation()).isEqualTo(conversationBack);

        conversationParticipant.conversation(null);
        assertThat(conversationParticipant.getConversation()).isNull();
    }
}
