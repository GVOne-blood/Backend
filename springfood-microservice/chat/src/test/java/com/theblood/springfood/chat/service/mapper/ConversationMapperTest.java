package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.ConversationAsserts.*;
import static com.theblood.springfood.chat.domain.ConversationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversationMapperTest {

    private ConversationMapper conversationMapper;

    @BeforeEach
    void setUp() {
        conversationMapper = new ConversationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getConversationSample1();
        var actual = conversationMapper.toEntity(conversationMapper.toDto(expected));
        assertConversationAllPropertiesEquals(expected, actual);
    }
}
