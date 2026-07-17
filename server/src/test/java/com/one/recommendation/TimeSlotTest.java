package com.one.recommendation;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSlotTest {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    @Test
    void shouldResolveMealAndDrinkTimeSlots() {
        assertThat(TimeSlot.at(ZonedDateTime.of(2026, 7, 17, 8, 0, 0, 0, SHANGHAI))).isEqualTo(TimeSlot.BREAKFAST);
        assertThat(TimeSlot.at(ZonedDateTime.of(2026, 7, 17, 12, 0, 0, 0, SHANGHAI))).isEqualTo(TimeSlot.LUNCH);
        assertThat(TimeSlot.at(ZonedDateTime.of(2026, 7, 17, 15, 0, 0, 0, SHANGHAI))).isEqualTo(TimeSlot.AFTERNOON);
        assertThat(TimeSlot.at(ZonedDateTime.of(2026, 7, 17, 19, 0, 0, 0, SHANGHAI))).isEqualTo(TimeSlot.DINNER);
        assertThat(TimeSlot.at(ZonedDateTime.of(2026, 7, 17, 23, 0, 0, 0, SHANGHAI))).isEqualTo(TimeSlot.LATE_NIGHT);
    }
}
