package com.theblood.springfood.chat.domain;

import java.util.UUID;

public class MessageReadReceiptTestSamples {

    public static MessageReadReceipt getMessageReadReceiptSample1() {
        return new MessageReadReceipt().receiptId("receiptId1").userId("userId1").deviceType("deviceType1");
    }

    public static MessageReadReceipt getMessageReadReceiptSample2() {
        return new MessageReadReceipt().receiptId("receiptId2").userId("userId2").deviceType("deviceType2");
    }

    public static MessageReadReceipt getMessageReadReceiptRandomSampleGenerator() {
        return new MessageReadReceipt()
            .receiptId(UUID.randomUUID().toString())
            .userId(UUID.randomUUID().toString())
            .deviceType(UUID.randomUUID().toString());
    }
}
