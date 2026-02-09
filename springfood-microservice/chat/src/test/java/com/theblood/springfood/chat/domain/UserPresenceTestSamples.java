package com.theblood.springfood.chat.domain;

import java.util.UUID;

public class UserPresenceTestSamples {

    public static UserPresence getUserPresenceSample1() {
        return new UserPresence()
            .presenceId("presenceId1")
            .userId("userId1")
            .status("status1")
            .statusMessage("statusMessage1")
            .activeConversationId("activeConversationId1")
            .deviceType("deviceType1")
            .deviceId("deviceId1")
            .sessionId("sessionId1");
    }

    public static UserPresence getUserPresenceSample2() {
        return new UserPresence()
            .presenceId("presenceId2")
            .userId("userId2")
            .status("status2")
            .statusMessage("statusMessage2")
            .activeConversationId("activeConversationId2")
            .deviceType("deviceType2")
            .deviceId("deviceId2")
            .sessionId("sessionId2");
    }

    public static UserPresence getUserPresenceRandomSampleGenerator() {
        return new UserPresence()
            .presenceId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString())
            .statusMessage(UUID.randomUUID().toString())
            .activeConversationId(UUID.randomUUID().toString())
            .deviceType(UUID.randomUUID().toString())
            .deviceId(UUID.randomUUID().toString())
            .sessionId(UUID.randomUUID().toString());
    }
}
