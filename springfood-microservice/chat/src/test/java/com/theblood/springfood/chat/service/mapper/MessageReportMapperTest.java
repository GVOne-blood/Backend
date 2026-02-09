package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.MessageReportAsserts.*;
import static com.theblood.springfood.chat.domain.MessageReportTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageReportMapperTest {

    private MessageReportMapper messageReportMapper;

    @BeforeEach
    void setUp() {
        messageReportMapper = new MessageReportMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMessageReportSample1();
        var actual = messageReportMapper.toEntity(messageReportMapper.toDto(expected));
        assertMessageReportAllPropertiesEquals(expected, actual);
    }
}
