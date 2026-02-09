package com.theblood.springfood.chat.service.mapper;

import static com.theblood.springfood.chat.domain.BlockedUserAsserts.*;
import static com.theblood.springfood.chat.domain.BlockedUserTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlockedUserMapperTest {

    private BlockedUserMapper blockedUserMapper;

    @BeforeEach
    void setUp() {
        blockedUserMapper = new BlockedUserMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBlockedUserSample1();
        var actual = blockedUserMapper.toEntity(blockedUserMapper.toDto(expected));
        assertBlockedUserAllPropertiesEquals(expected, actual);
    }
}
