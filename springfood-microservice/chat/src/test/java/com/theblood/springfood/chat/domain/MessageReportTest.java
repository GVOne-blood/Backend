package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.MessageReportTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageReportTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageReport.class);
        MessageReport messageReport1 = getMessageReportSample1();
        MessageReport messageReport2 = new MessageReport();
        assertThat(messageReport1).isNotEqualTo(messageReport2);

        messageReport2.setReportId(messageReport1.getReportId());
        assertThat(messageReport1).isEqualTo(messageReport2);

        messageReport2 = getMessageReportSample2();
        assertThat(messageReport1).isNotEqualTo(messageReport2);
    }
}
