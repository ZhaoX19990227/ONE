package com.one.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "drink_record_detail")
public class DrinkRecordDetail {
    @Id @Column(name = "record_id") private Long recordId;
    @Enumerated(EnumType.STRING) @Column(name = "sugar_level", length = 20) private SugarLevel sugarLevel;
    @Enumerated(EnumType.STRING) @Column(name = "ice_level", length = 20) private IceLevel iceLevel;
    @Column(name = "cup_size", length = 20) private String cupSize;
    @Column(columnDefinition = "json") private String toppings;
    @Enumerated(EnumType.STRING) @Column(name = "taste_feedback", length = 24) private TasteFeedback tasteFeedback;
    @Enumerated(EnumType.STRING) @Column(name = "repurchase_intent", length = 20) private RepurchaseIntent repurchaseIntent;
    protected DrinkRecordDetail() {}
    public static DrinkRecordDetail create(long recordId, SugarLevel sugar, IceLevel ice, String cupSize,
                                           String toppings, TasteFeedback feedback, RepurchaseIntent repurchase) {
        DrinkRecordDetail value = new DrinkRecordDetail(); value.recordId = recordId; value.sugarLevel = sugar;
        value.iceLevel = ice; value.cupSize = cupSize; value.toppings = toppings;
        value.tasteFeedback = feedback; value.repurchaseIntent = repurchase; return value;
    }
    public SugarLevel getSugarLevel() { return sugarLevel; }
    public IceLevel getIceLevel() { return iceLevel; }
    public TasteFeedback getTasteFeedback() { return tasteFeedback; }
    public RepurchaseIntent getRepurchaseIntent() { return repurchaseIntent; }
}
