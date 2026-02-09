package com.theblood.springfood.chat.domain;

import java.util.UUID;

public class MessageReactionTestSamples {

    public static MessageReaction getMessageReactionSample1() {
        return new MessageReaction().reactionId("reactionId1").userId("userId1").emoji("emoji1").emojiDisplay("emojiDisplay1");
    }

    public static MessageReaction getMessageReactionSample2() {
        return new MessageReaction().reactionId("reactionId2").userId("userId2").emoji("emoji2").emojiDisplay("emojiDisplay2");
    }

    public static MessageReaction getMessageReactionRandomSampleGenerator() {
        return new MessageReaction()
            .reactionId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .emoji(UUID.randomUUID().toString())
            .emojiDisplay(UUID.randomUUID().toString());
    }
}
