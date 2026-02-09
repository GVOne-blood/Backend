package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.MessageReactionAsserts.*;
import static com.theblood.springfood.chat.domain.MessageReactionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageReactionMapperTest {

    private MessageReactionMapper messageReactionMapper;

    @BeforeEach
    void setUp() {
        messageReactionMapper = new MessageReactionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMessageReactionSample1();
        var actual = messageReactionMapper.toEntity(messageReactionMapper.toDto(expected));
        assertMessageReactionAllPropertiesEquals(expected, actual);
    }
}
