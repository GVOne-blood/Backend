package com.theblood.springfood.chat.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TypingIndicatorDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TypingIndicatorDTO.class);
        TypingIndicatorDTO typingIndicatorDTO1 = new TypingIndicatorDTO();
        typingIndicatorDTO1.setIndicatorId("id1");
        TypingIndicatorDTO typingIndicatorDTO2 = new TypingIndicatorDTO();
        assertThat(typingIndicatorDTO1).isNotEqualTo(typingIndicatorDTO2);
        typingIndicatorDTO2.setIndicatorId(typingIndicatorDTO1.getIndicatorId());
        assertThat(typingIndicatorDTO1).isEqualTo(typingIndicatorDTO2);
        typingIndicatorDTO2.setIndicatorId("id2");
        assertThat(typingIndicatorDTO1).isNotEqualTo(typingIndicatorDTO2);
        typingIndicatorDTO1.setIndicatorId(null);
        assertThat(typingIndicatorDTO1).isNotEqualTo(typingIndicatorDTO2);
    }
}
