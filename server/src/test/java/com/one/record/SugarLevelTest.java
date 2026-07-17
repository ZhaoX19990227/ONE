package com.one.record;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SugarLevelTest {
    @Test
    void shouldLowerFiveSugarToThreeAfterSweetFeedback() {
        assertThat(SugarLevel.FIVE.oneStepLower()).isEqualTo(SugarLevel.THREE);
    }

    @Test
    void shouldNeverLowerBelowNoSugar() {
        assertThat(SugarLevel.NO_SUGAR.oneStepLower()).isEqualTo(SugarLevel.NO_SUGAR);
    }
}
