package com.theblood.springfood.chat.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ConversationSettingsTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static ConversationSettings getConversationSettingsSample1() {
        return new ConversationSettings()
            .settingsId("settingsId1")
            .onlyAdminCanSend(1)
            .onlyAdminCanAddMembers(1)
            .autoDeleteDays(1)
            .allowReactions(1)
            .allowReplies(1)
            .allowAttachments(1)
            .maxAttachmentSizeMb(1)
            .allowedFileTypes("allowedFileTypes1")
            .showReadReceipts(1)
            .showTypingIndicators(1);
    }

    public static ConversationSettings getConversationSettingsSample2() {
        return new ConversationSettings()
            .settingsId("settingsId2")
            .onlyAdminCanSend(2)
            .onlyAdminCanAddMembers(2)
            .autoDeleteDays(2)
            .allowReactions(2)
            .allowReplies(2)
            .allowAttachments(2)
            .maxAttachmentSizeMb(2)
            .allowedFileTypes("allowedFileTypes2")
            .showReadReceipts(2)
            .showTypingIndicators(2);
    }

    public static ConversationSettings getConversationSettingsRandomSampleGenerator() {
        return new ConversationSettings()
            .settingsId(UUID.randomUUID().toString())
            .onlyAdminCanSend(intCount.incrementAndGet())
            .onlyAdminCanAddMembers(intCount.incrementAndGet())
            .autoDeleteDays(intCount.incrementAndGet())
            .allowReactions(intCount.incrementAndGet())
            .allowReplies(intCount.incrementAndGet())
            .allowAttachments(intCount.incrementAndGet())
            .maxAttachmentSizeMb(intCount.incrementAndGet())
            .allowedFileTypes(UUID.randomUUID().toString())
            .showReadReceipts(intCount.incrementAndGet())
            .showTypingIndicators(intCount.incrementAndGet());
    }
}
