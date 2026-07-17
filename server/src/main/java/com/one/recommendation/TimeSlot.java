package com.one.recommendation;

import java.time.ZonedDateTime;

public enum TimeSlot {
    BREAKFAST,
    LUNCH,
    AFTERNOON,
    DINNER,
    LATE_NIGHT;

    public static TimeSlot at(ZonedDateTime time) {
        int hour = time.getHour();
        if (hour < 10) return BREAKFAST;
        if (hour < 14) return LUNCH;
        if (hour < 17) return AFTERNOON;
        if (hour < 22) return DINNER;
        return LATE_NIGHT;
    }
}
