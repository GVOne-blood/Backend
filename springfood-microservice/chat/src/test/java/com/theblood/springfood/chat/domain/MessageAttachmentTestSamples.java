package com.theblood.springfood.chat.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MessageAttachmentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static MessageAttachment getMessageAttachmentSample1() {
        return new MessageAttachment()
            .attachmentId("attachmentId1")
            .mediaId("mediaId1")
            .attachmentType("attachmentType1")
            .fileName("fileName1")
            .fileSize(1L)
            .mimeType("mimeType1")
            .url("url1")
            .thumbnailUrl("thumbnailUrl1")
            .width(1)
            .height(1)
            .duration(1)
            .displayOrder(1);
    }

    public static MessageAttachment getMessageAttachmentSample2() {
        return new MessageAttachment()
            .attachmentId("attachmentId2")
            .mediaId("mediaId2")
            .attachmentType("attachmentType2")
            .fileName("fileName2")
            .fileSize(2L)
            .mimeType("mimeType2")
            .url("url2")
            .thumbnailUrl("thumbnailUrl2")
            .width(2)
            .height(2)
            .duration(2)
            .displayOrder(2);
    }

    public static MessageAttachment getMessageAttachmentRandomSampleGenerator() {
        return new MessageAttachment()
            .attachmentId(UUID.randomUUID().toString())
            .mediaId(UUID.randomUUID().toString())
            .attachmentType(UUID.randomUUID().toString())
            .fileName(UUID.randomUUID().toString())
            .fileSize(longCount.incrementAndGet())
            .mimeType(UUID.randomUUID().toString())
            .url(UUID.randomUUID().toString())
            .thumbnailUrl(UUID.randomUUID().toString())
            .width(intCount.incrementAndGet())
            .height(intCount.incrementAndGet())
            .duration(intCount.incrementAndGet())
            .displayOrder(intCount.incrementAndGet());
    }
}
