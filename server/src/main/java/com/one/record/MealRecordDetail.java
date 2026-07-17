package com.one.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_record_detail")
public class MealRecordDetail {
    @Id @Column(name = "record_id") private Long recordId;
    @Column(name = "dining_mode", length = 20) private String diningMode;
    @Column(name = "hunger_level", length = 20) private String hungerLevel;
    @Enumerated(EnumType.STRING) @Column(name = "taste_feedback", length = 24) private TasteFeedback tasteFeedback;
    @Column(length = 20) private String satiety;
    @Enumerated(EnumType.STRING) @Column(name = "repurchase_intent", length = 20) private RepurchaseIntent repurchaseIntent;
    protected MealRecordDetail() {}
    public static MealRecordDetail create(long recordId, String diningMode, String hungerLevel,
                                          TasteFeedback feedback, String satiety, RepurchaseIntent repurchase) {
        MealRecordDetail value = new MealRecordDetail(); value.recordId = recordId; value.diningMode = diningMode;
        value.hungerLevel = hungerLevel; value.tasteFeedback = feedback; value.satiety = satiety;
        value.repurchaseIntent = repurchase; return value;
    }
    public TasteFeedback getTasteFeedback() { return tasteFeedback; }
    public RepurchaseIntent getRepurchaseIntent() { return repurchaseIntent; }
}
