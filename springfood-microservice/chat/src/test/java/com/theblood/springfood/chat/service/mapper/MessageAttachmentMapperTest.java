package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.MessageAttachmentAsserts.*;
import static com.theblood.springfood.chat.domain.MessageAttachmentTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageAttachmentMapperTest {

    private MessageAttachmentMapper messageAttachmentMapper;

    @BeforeEach
    void setUp() {
        messageAttachmentMapper = new MessageAttachmentMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMessageAttachmentSample1();
        var actual = messageAttachmentMapper.toEntity(messageAttachmentMapper.toDto(expected));
        assertMessageAttachmentAllPropertiesEquals(expected, actual);
    }
}
