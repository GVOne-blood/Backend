package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.UserPresenceAsserts.*;
import static com.theblood.springfood.chat.domain.UserPresenceTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPresenceMapperTest {

    private UserPresenceMapper userPresenceMapper;

    @BeforeEach
    void setUp() {
        userPresenceMapper = new UserPresenceMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getUserPresenceSample1();
        var actual = userPresenceMapper.toEntity(userPresenceMapper.toDto(expected));
        assertUserPresenceAllPropertiesEquals(expected, actual);
    }
}
