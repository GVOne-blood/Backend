package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.ConversationParticipantAsserts.*;
import static com.theblood.springfood.chat.domain.ConversationParticipantTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversationParticipantMapperTest {

    private ConversationParticipantMapper conversationParticipantMapper;

    @BeforeEach
    void setUp() {
        conversationParticipantMapper = new ConversationParticipantMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getConversationParticipantSample1();
        var actual = conversationParticipantMapper.toEntity(conversationParticipantMapper.toDto(expected));
        assertConversationParticipantAllPropertiesEquals(expected, actual);
    }
}
