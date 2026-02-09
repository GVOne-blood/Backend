package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.MessageReadReceiptAsserts.*;
import static com.theblood.springfood.chat.domain.MessageReadReceiptTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageReadReceiptMapperTest {

    private MessageReadReceiptMapper messageReadReceiptMapper;

    @BeforeEach
    void setUp() {
        messageReadReceiptMapper = new MessageReadReceiptMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMessageReadReceiptSample1();
        var actual = messageReadReceiptMapper.toEntity(messageReadReceiptMapper.toDto(expected));
        assertMessageReadReceiptAllPropertiesEquals(expected, actual);
    }
}
