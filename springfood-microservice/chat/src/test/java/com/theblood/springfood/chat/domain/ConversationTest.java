package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.ConversationParticipantTestSamples.*;
import static com.theblood.springfood.chat.domain.ConversationSettingsTestSamples.*;
import static com.theblood.springfood.chat.domain.ConversationTestSamples.*;
import static com.theblood.springfood.chat.domain.MessageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConversationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Conversation.class);
        Conversation conversation1 = getConversationSample1();
        Conversation conversation2 = new Conversation();
        assertThat(conversation1).isNotEqualTo(conversation2);

        conversation2.setConversationId(conversation1.getConversationId());
        assertThat(conversation1).isEqualTo(conversation2);

        conversation2 = getConversationSample2();
        assertThat(conversation1).isNotEqualTo(conversation2);
    }

    @Test
    void settingsTest() {
        Conversation conversation = getConversationRandomSampleGenerator();
        ConversationSettings conversationSettingsBack = getConversationSettingsRandomSampleGenerator();

        conversation.setSettings(conversationSettingsBack);
        assertThat(conversation.getSettings()).isEqualTo(conversationSettingsBack);

        conversation.settings(null);
        assertThat(conversation.getSettings()).isNull();
    }

    @Test
    void participantsTest() {
        Conversation conversation = getConversationRandomSampleGenerator();
        ConversationParticipant conversationParticipantBack = getConversationParticipantRandomSampleGenerator();

        conversation.addParticipants(conversationParticipantBack);
        assertThat(conversation.getParticipants()).containsOnly(conversationParticipantBack);
        assertThat(conversationParticipantBack.getConversation()).isEqualTo(conversation);

        conversation.removeParticipants(conversationParticipantBack);
        assertThat(conversation.getParticipants()).doesNotContain(conversationParticipantBack);
        assertThat(conversationParticipantBack.getConversation()).isNull();

        conversation.participants(new HashSet<>(Set.of(conversationParticipantBack)));
        assertThat(conversation.getParticipants()).containsOnly(conversationParticipantBack);
        assertThat(conversationParticipantBack.getConversation()).isEqualTo(conversation);

        conversation.setParticipants(new HashSet<>());
        assertThat(conversation.getParticipants()).doesNotContain(conversationParticipantBack);
        assertThat(conversationParticipantBack.getConversation()).isNull();
    }

    @Test
    void messagesTest() {
        Conversation conversation = getConversationRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        conversation.addMessages(messageBack);
        assertThat(conversation.getMessages()).containsOnly(messageBack);
        assertThat(messageBack.getConversation()).isEqualTo(conversation);

        conversation.removeMessages(messageBack);
        assertThat(conversation.getMessages()).doesNotContain(messageBack);
        assertThat(messageBack.getConversation()).isNull();

        conversation.messages(new HashSet<>(Set.of(messageBack)));
        assertThat(conversation.getMessages()).containsOnly(messageBack);
        assertThat(messageBack.getConversation()).isEqualTo(conversation);

        conversation.setMessages(new HashSet<>());
        assertThat(conversation.getMessages()).doesNotContain(messageBack);
        assertThat(messageBack.getConversation()).isNull();
    }
}
