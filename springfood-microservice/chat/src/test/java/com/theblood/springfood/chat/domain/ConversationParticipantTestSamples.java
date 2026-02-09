package com.theblood.springfood.chat.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ConversationParticipantTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static ConversationParticipant getConversationParticipantSample1() {
        return new ConversationParticipant()
            .participantId("participantId1")
            .userId("userId1")
            .displayName("displayName1")
            .avatarUrl("avatarUrl1")
            .role("role1")
            .status("status1")
            .nickname("nickname1")
            .lastReadMessageId("lastReadMessageId1")
            .unreadCount(1)
            .isMuted(1)
            .isPinned(1)
            .addedBy("addedBy1");
    }

    public static ConversationParticipant getConversationParticipantSample2() {
        return new ConversationParticipant()
            .participantId("participantId2")
            .userId("userId2")
            .displayName("displayName2")
            .avatarUrl("avatarUrl2")
            .role("role2")
            .status("status2")
            .nickname("nickname2")
            .lastReadMessageId("lastReadMessageId2")
            .unreadCount(2)
            .isMuted(2)
            .isPinned(2)
            .addedBy("addedBy2");
    }

    public static ConversationParticipant getConversationParticipantRandomSampleGenerator() {
        return new ConversationParticipant()
            .participantId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .displayName(UUID.randomUUID().toString())
            .avatarUrl(UUID.randomUUID().toString())
            .role(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString())
            .nickname(UUID.randomUUID().toString())
            .lastReadMessageId(UUID.randomUUID().toString())
            .unreadCount(intCount.incrementAndGet())
            .isMuted(intCount.incrementAndGet())
            .isPinned(intCount.incrementAndGet())
            .addedBy(UUID.randomUUID().toString());
    }
}
