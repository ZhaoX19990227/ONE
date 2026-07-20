package com.one.recommendation;

import com.one.common.Dimension;

import java.util.List;
import java.util.Optional;

public interface RecommendationAiAdvisor {
    Optional<Advice> advise(Dimension dimension, TimeSlot timeSlot, List<Option> options);

    record Option(long itemId, String itemName, String brandName, String localReason) {}
    record RankedItem(long itemId, String reason) {}
    record Advice(String openingLine, List<RankedItem> items) {}
}
