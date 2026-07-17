package com.one.recommendation;

import com.one.common.Dimension;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class RecommendationDtos {
    private RecommendationDtos() {}

    public record Request(@NotNull Dimension dimension, @NotNull DecisionMode mode,
                          @Min(0) @Max(100_000) Integer budgetMaxFen) {}

    public record Candidate(long id, int position, long categoryId, String categoryName,
                            Long brandId, String brandName, String brandShortName,
                            String brandLogoUrl, String brandColor,
                            long itemId, String itemName, String imageUrl, Integer defaultPriceFen,
                            String reason, String suggestionJson, boolean chosen) {}

    public record View(long sessionId, Dimension dimension, DecisionMode mode, TimeSlot timeSlot,
                       DecisionStatus status, Long chosenCandidateId, String openingLine,
                       List<Candidate> candidates) {}
}
