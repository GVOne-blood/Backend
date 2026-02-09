package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.TypingIndicatorAsserts.*;
import static com.theblood.springfood.chat.domain.TypingIndicatorTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypingIndicatorMapperTest {

    private TypingIndicatorMapper typingIndicatorMapper;

    @BeforeEach
    void setUp() {
        typingIndicatorMapper = new TypingIndicatorMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTypingIndicatorSample1();
        var actual = typingIndicatorMapper.toEntity(typingIndicatorMapper.toDto(expected));
        assertTypingIndicatorAllPropertiesEquals(expected, actual);
    }
}
