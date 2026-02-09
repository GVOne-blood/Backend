package com.theblood.springfood.chat.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Message getMessageSample1() {
        return new Message()
            .messageId("messageId1")
            .clientMessageId("clientMessageId1")
            .senderId("senderId1")
            .senderName("senderName1")
            .senderAvatar("senderAvatar1")
            .messageType("messageType1")
            .contentPreview("contentPreview1")
            .replyToMessageId("replyToMessageId1")
            .replyToPreview("replyToPreview1")
            .forwardedFromMessageId("forwardedFromMessageId1")
            .forwardedFromConversationId("forwardedFromConversationId1")
            .referenceType("referenceType1")
            .referenceId("referenceId1")
            .status("status1")
            .isEdited(1)
            .isDeleted(1)
            .deletedBy("deletedBy1")
            .reactionCount(1);
    }

    public static Message getMessageSample2() {
        return new Message()
            .messageId("messageId2")
            .clientMessageId("clientMessageId2")
            .senderId("senderId2")
            .senderName("senderName2")
            .senderAvatar("senderAvatar2")
            .messageType("messageType2")
            .contentPreview("contentPreview2")
            .replyToMessageId("replyToMessageId2")
            .replyToPreview("replyToPreview2")
            .forwardedFromMessageId("forwardedFromMessageId2")
            .forwardedFromConversationId("forwardedFromConversationId2")
            .referenceType("referenceType2")
            .referenceId("referenceId2")
            .status("status2")
            .isEdited(2)
            .isDeleted(2)
            .deletedBy("deletedBy2")
            .reactionCount(2);
    }

    public static Message getMessageRandomSampleGenerator() {
        return new Message()
            .messageId(UUID.randomUUID().toString())
            .clientMessageId(UUID.randomUUID().toString())
            .senderId(UUID.randomUUID().toString())
            .senderName(UUID.randomUUID().toString())
            .senderAvatar(UUID.randomUUID().toString())
            .messageType(UUID.randomUUID().toString())
            .contentPreview(UUID.randomUUID().toString())
            .replyToMessageId(UUID.randomUUID().toString())
            .replyToPreview(UUID.randomUUID().toString())
            .forwardedFromMessageId(UUID.randomUUID().toString())
            .forwardedFromConversationId(UUID.randomUUID().toString())
            .referenceType(UUID.randomUUID().toString())
            .referenceId(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString())
            .isEdited(intCount.incrementAndGet())
            .isDeleted(intCount.incrementAndGet())
            .deletedBy(UUID.randomUUID().toString())
            .reactionCount(intCount.incrementAndGet());
    }
}
