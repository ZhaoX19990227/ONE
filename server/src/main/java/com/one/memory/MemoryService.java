package com.one.memory;

import com.one.record.DrinkRecordDetail;
import com.one.record.LifeRecord;
import com.one.record.MealRecordDetail;
import com.one.record.RepurchaseIntent;
import com.one.record.SugarLevel;
import com.one.record.TasteFeedback;
import com.one.common.BusinessException;
import com.one.common.Dimension;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemoryService {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("M月d日");
    private final PreferenceMemoryRepository memories;

    public MemoryService(PreferenceMemoryRepository memories) { this.memories = memories; }

    public List<PreferenceMemory> rememberMeal(LifeRecord record, MealRecordDetail detail) {
        List<PreferenceMemory> created = new ArrayList<>();
        addTasteMemory(record, detail.getTasteFeedback(), null, created);
        addRepurchase(record, detail.getRepurchaseIntent(), created);
        return memories.saveAll(created);
    }

    public List<PreferenceMemory> rememberDrink(LifeRecord record, DrinkRecordDetail detail) {
        List<PreferenceMemory> created = new ArrayList<>();
        addTasteMemory(record, detail.getTasteFeedback(), detail.getSugarLevel(), created);
        addRepurchase(record, detail.getRepurchaseIntent(), created);
        return memories.saveAll(created);
    }

    @Transactional(readOnly = true)
    public List<MemoryDtos.View> list(long userId, Dimension dimension) {
        List<PreferenceMemory> values = dimension == null
                ? memories.findTop100ByUserIdAndActiveTrueOrderBySourceAtDesc(userId)
                : memories.findTop100ByUserIdAndDimensionAndActiveTrueOrderBySourceAtDesc(userId, dimension);
        return values.stream().map(MemoryDtos.View::from).toList();
    }

    @Transactional
    public void forget(long userId, long memoryId) {
        PreferenceMemory memory = memories.findByIdAndUserIdAndActiveTrue(memoryId, userId)
                .orElseThrow(() -> new BusinessException("MEMORY_NOT_FOUND", "这条记忆已经不存在", HttpStatus.NOT_FOUND));
        memory.forget();
    }

    public void forgetByRecord(long userId, long recordId) {
        memories.findByUserIdAndSourceRecordIdAndActiveTrue(userId, recordId)
                .forEach(PreferenceMemory::forget);
    }

    private void addTasteMemory(LifeRecord record, TasteFeedback feedback, SugarLevel sugar,
                                List<PreferenceMemory> target) {
        if (feedback == null || feedback == TasteFeedback.JUST_RIGHT) return;
        String date = DATE.format(record.getOccurredAt().atZone(SHANGHAI));
        String subject = subject(record);
        MemorySignal signal;
        String suggestion = null;
        String attribute = "taste";
        String text;
        switch (feedback) {
            case A_BIT_SWEET, TOO_SWEET -> {
                signal = feedback == TasteFeedback.A_BIT_SWEET ? MemorySignal.A_BIT_SWEET : MemorySignal.TOO_SWEET;
                suggestion = sugar == null ? null : sugar.oneStepLower().name();
                attribute = "sugarLevel";
                text = date + "你觉得" + subject + sugarText(sugar) + (feedback == TasteFeedback.A_BIT_SWEET ? "有点甜" : "太甜了");
            }
            case A_BIT_SALTY -> { signal = MemorySignal.A_BIT_SALTY; text = date + "你觉得" + subject + "有点咸"; }
            case TOO_SALTY -> { signal = MemorySignal.TOO_SALTY; text = date + "你觉得" + subject + "太咸"; }
            case TOO_SPICY -> { signal = MemorySignal.TOO_SPICY; text = date + "你觉得" + subject + "太辣"; }
            case TOO_BITTER -> { signal = MemorySignal.TOO_BITTER; text = date + "你觉得" + subject + "偏苦"; }
            default -> { signal = MemorySignal.DISLIKE; text = date + "你觉得" + subject + "不太合口味"; }
        }
        target.add(PreferenceMemory.from(record, signal, attribute, sugar == null ? feedback.name() : sugar.name(),
                suggestion, text, feedback == TasteFeedback.TOO_SWEET ? 130 : 100));
    }

    private void addRepurchase(LifeRecord record, RepurchaseIntent repurchase, List<PreferenceMemory> target) {
        if (repurchase == null) return;
        String date = DATE.format(record.getOccurredAt().atZone(SHANGHAI));
        if (repurchase == RepurchaseIntent.YES) {
            target.add(PreferenceMemory.from(record, MemorySignal.REPURCHASE, "repurchase", "YES", null,
                    date + "你把" + subject(record) + "标记为还会再来", 140));
        } else if (repurchase == RepurchaseIntent.NO) {
            target.add(PreferenceMemory.from(record, MemorySignal.DISLIKE, "repurchase", "NO", null,
                    date + "你觉得" + subject(record) + "下次不会再见", 120));
        }
    }

    private String subject(LifeRecord record) {
        String brand = record.getBrandNameSnapshot() == null ? "" : record.getBrandNameSnapshot() + "的";
        return brand + record.getItemNameSnapshot();
    }

    private String sugarText(SugarLevel sugar) {
        if (sugar == null || sugar == SugarLevel.UNKNOWN) return "";
        return switch (sugar) {
            case NO_SUGAR -> "无糖"; case LOW -> "少少甜"; case THREE -> "三分糖";
            case FIVE -> "五分糖"; case SEVEN -> "七分糖"; case FULL -> "全糖";
            case NORMAL -> "标准甜"; default -> "";
        };
    }
}
