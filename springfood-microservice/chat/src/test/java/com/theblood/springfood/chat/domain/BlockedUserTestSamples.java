package com.theblood.springfood.chat.domain;

import java.util.UUID;

public class BlockedUserTestSamples {

    public static BlockedUser getBlockedUserSample1() {
        return new BlockedUser().blockId("blockId1").blockerId("blockerId1").blockedUserId("blockedUserId1").reason("reason1");
    }

    public static BlockedUser getBlockedUserSample2() {
        return new BlockedUser().blockId("blockId2").blockerId("blockerId2").blockedUserId("blockedUserId2").reason("reason2");
    }

    public static BlockedUser getBlockedUserRandomSampleGenerator() {
        return new BlockedUser()
            .blockId(UUID.randomUUID().toString())
            .blockerId(UUID.randomUUID().toString())
            .blockedUserId(UUID.randomUUID().toString())
            .reason(UUID.randomUUID().toString());
    }
}
