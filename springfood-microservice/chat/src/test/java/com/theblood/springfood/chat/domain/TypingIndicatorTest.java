package com.theblood.springfood.chat.domain;

import static com.theblood.springfood.chat.domain.TypingIndicatorTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.springfood.chat.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TypingIndicatorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TypingIndicator.class);
        TypingIndicator typingIndicator1 = getTypingIndicatorSample1();
        TypingIndicator typingIndicator2 = new TypingIndicator();
        assertThat(typingIndicator1).isNotEqualTo(typingIndicator2);

        typingIndicator2.setIndicatorId(typingIndicator1.getIndicatorId());
        assertThat(typingIndicator1).isEqualTo(typingIndicator2);

        typingIndicator2 = getTypingIndicatorSample2();
        assertThat(typingIndicator1).isNotEqualTo(typingIndicator2);
    }
}
