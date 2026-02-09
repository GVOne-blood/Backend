package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.ConversationSettingsTestSamples.*;
import static com.theblood.springfood.chat.domain.ConversationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ConversationSettingsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ConversationSettings.class);
        ConversationSettings conversationSettings1 = getConversationSettingsSample1();
        ConversationSettings conversationSettings2 = new ConversationSettings();
        assertThat(conversationSettings1).isNotEqualTo(conversationSettings2);

        conversationSettings2.setSettingsId(conversationSettings1.getSettingsId());
        assertThat(conversationSettings1).isEqualTo(conversationSettings2);

        conversationSettings2 = getConversationSettingsSample2();
        assertThat(conversationSettings1).isNotEqualTo(conversationSettings2);
    }

    @Test
    void conversationTest() {
        ConversationSettings conversationSettings = getConversationSettingsRandomSampleGenerator();
        Conversation conversationBack = getConversationRandomSampleGenerator();

        conversationSettings.setConversation(conversationBack);
        assertThat(conversationSettings.getConversation()).isEqualTo(conversationBack);
        assertThat(conversationBack.getSettings()).isEqualTo(conversationSettings);

        conversationSettings.conversation(null);
        assertThat(conversationSettings.getConversation()).isNull();
        assertThat(conversationBack.getSettings()).isNull();
    }
}
