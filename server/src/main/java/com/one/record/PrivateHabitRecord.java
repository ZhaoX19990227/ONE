package com.one.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "private_habit_record")
public class PrivateHabitRecord {
    @Id @Column(name = "record_id") private Long recordId;
    @Column(name = "ordinal_of_day", nullable = false) private int ordinalOfDay;
    @Enumerated(EnumType.STRING) @Column(name = "body_feeling", length = 20) private BodyFeeling bodyFeeling;
    protected PrivateHabitRecord() {}
    public static PrivateHabitRecord create(long recordId, int ordinal, BodyFeeling feeling) {
        PrivateHabitRecord value = new PrivateHabitRecord(); value.recordId = recordId;
        value.ordinalOfDay = ordinal; value.bodyFeeling = feeling; return value;
    }
    public int getOrdinalOfDay() { return ordinalOfDay; }
    public BodyFeeling getBodyFeeling() { return bodyFeeling; }
}
