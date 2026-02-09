package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MessageReportDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MessageReportDTO.class);
        MessageReportDTO messageReportDTO1 = new MessageReportDTO();
        messageReportDTO1.setReportId("id1");
        MessageReportDTO messageReportDTO2 = new MessageReportDTO();
        assertThat(messageReportDTO1).isNotEqualTo(messageReportDTO2);
        messageReportDTO2.setReportId(messageReportDTO1.getReportId());
        assertThat(messageReportDTO1).isEqualTo(messageReportDTO2);
        messageReportDTO2.setReportId("id2");
        assertThat(messageReportDTO1).isNotEqualTo(messageReportDTO2);
        messageReportDTO1.setReportId(null);
        assertThat(messageReportDTO1).isNotEqualTo(messageReportDTO2);
    }
}
