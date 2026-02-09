package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.ConversationSettingsAsserts.*;
import static com.theblood.springfood.chat.domain.ConversationSettingsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversationSettingsMapperTest {

    private ConversationSettingsMapper conversationSettingsMapper;

    @BeforeEach
    void setUp() {
        conversationSettingsMapper = new ConversationSettingsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getConversationSettingsSample1();
        var actual = conversationSettingsMapper.toEntity(conversationSettingsMapper.toDto(expected));
        assertConversationSettingsAllPropertiesEquals(expected, actual);
    }
}
