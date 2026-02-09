package com.theblood.springfood.chat.domain;

import java.util.UUID;

public class MessageReportTestSamples {

    public static MessageReport getMessageReportSample1() {
        return new MessageReport()
            .reportId("reportId1")
            .reporterId("reporterId1")
            .messageId("messageId1")
            .reason("reason1")
            .details("details1")
            .status("status1")
            .reviewedBy("reviewedBy1")
            .reviewNotes("reviewNotes1")
            .actionTaken("actionTaken1");
    }

    public static MessageReport getMessageReportSample2() {
        return new MessageReport()
            .reportId("reportId2")
            .reporterId("reporterId2")
            .messageId("messageId2")
            .reason("reason2")
            .details("details2")
            .status("status2")
            .reviewedBy("reviewedBy2")
            .reviewNotes("reviewNotes2")
            .actionTaken("actionTaken2");
    }

    public static MessageReport getMessageReportRandomSampleGenerator() {
        return new MessageReport()
            .reportId(UUID.randomUUID().toString())
            .reporterId(UUID.randomUUID().toString())
            .messageId(UUID.randomUUID().toString())
            .reason(UUID.randomUUID().toString())
            .details(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString())
            .reviewedBy(UUID.randomUUID().toString())
            .reviewNotes(UUID.randomUUID().toString())
            .actionTaken(UUID.randomUUID().toString());
    }
}
