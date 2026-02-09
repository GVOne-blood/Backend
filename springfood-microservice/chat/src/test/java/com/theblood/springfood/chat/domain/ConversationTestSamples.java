package com.theblood.springfood.chat.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConversationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Conversation getConversationSample1() {
        return new Conversation()
            .conversationId("conversationId1")
            .conversationType("conversationType1")
            .name("name1")
            .description("description1")
            .avatarUrl("avatarUrl1")
            .referenceType("referenceType1")
            .referenceId("referenceId1")
            .lastMessagePreview("lastMessagePreview1")
            .lastMessageSenderId("lastMessageSenderId1")
            .messageCount(1L)
            .isArchived(1)
            .isPinned(1);
    }

    public static Conversation getConversationSample2() {
        return new Conversation()
            .conversationId("conversationId2")
            .conversationType("conversationType2")
            .name("name2")
            .description("description2")
            .avatarUrl("avatarUrl2")
            .referenceType("referenceType2")
            .referenceId("referenceId2")
            .lastMessagePreview("lastMessagePreview2")
            .lastMessageSenderId("lastMessageSenderId2")
            .messageCount(2L)
            .isArchived(2)
            .isPinned(2);
    }

    public static Conversation getConversationRandomSampleGenerator() {
        return new Conversation()
            .conversationId(UUID.randomUUID().toString())
            .conversationType(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .avatarUrl(UUID.randomUUID().toString())
            .referenceType(UUID.randomUUID().toString())
            .referenceId(UUID.randomUUID().toString())
            .lastMessagePreview(UUID.randomUUID().toString())
            .lastMessageSenderId(UUID.randomUUID().toString())
            .messageCount(longCount.incrementAndGet())
            .isArchived(intCount.incrementAndGet())
            .isPinned(intCount.incrementAndGet());
    }
}
