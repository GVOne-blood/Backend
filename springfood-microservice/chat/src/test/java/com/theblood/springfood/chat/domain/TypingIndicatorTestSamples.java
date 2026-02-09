package com.theblood.springfood.chat.domain;

import java.util.UUID;

public class TypingIndicatorTestSamples {

    public static TypingIndicator getTypingIndicatorSample1() {
        return new TypingIndicator().indicatorId("indicatorId1").userId("userId1").userName("userName1").conversationId("conversationId1");
    }

    public static TypingIndicator getTypingIndicatorSample2() {
        return new TypingIndicator().indicatorId("indicatorId2").userId("userId2").userName("userName2").conversationId("conversationId2");
    }

    public static TypingIndicator getTypingIndicatorRandomSampleGenerator() {
        return new TypingIndicator()
            .indicatorId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .userName(UUID.randomUUID().toString())
            .conversationId(UUID.randomUUID().toString());
    }
}
