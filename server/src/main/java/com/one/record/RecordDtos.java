package com.one.record;

import com.one.common.Dimension;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class RecordDtos {
    private RecordDtos() {}

    public record MoneyRequest(
            @Min(0) @Max(10_000_000) Integer originalAmountFen,
            @Min(0) @Max(10_000_000) Integer discountAmountFen,
            @Min(0) @Max(10_000_000) Integer actualAmountFen) {}

    public record MealRequest(
            @NotNull Instant occurredAt,
            Long decisionSessionId,
            Long categoryId,
            Long brandId,
            Long itemId,
            @Size(max = 80) String customBrandName,
            @Size(max = 100) String customItemName,
            @Size(max = 500) String thumbnailUrl,
            @Valid MoneyRequest money,
            @Min(1) @Max(5) Integer rating,
            @Size(max = 300) String note,
            RecordSource source,
            @Size(max = 20) String diningMode,
            @Size(max = 20) String hungerLevel,
            TasteFeedback tasteFeedback,
            @Size(max = 20) String satiety,
            RepurchaseIntent repurchaseIntent) {}

    public record DrinkRequest(
            @NotNull Dimension dimension,
            @NotNull Instant occurredAt,
            Long decisionSessionId,
            Long categoryId,
            Long brandId,
            Long itemId,
            @Size(max = 80) String customBrandName,
            @Size(max = 100) String customItemName,
            @Size(max = 500) String thumbnailUrl,
            @Valid MoneyRequest money,
            @Min(1) @Max(5) Integer rating,
            @Size(max = 300) String note,
            RecordSource source,
            SugarLevel sugarLevel,
            IceLevel iceLevel,
            @Size(max = 20) String cupSize,
            @Size(max = 12) List<@Size(max = 30) String> toppings,
            TasteFeedback tasteFeedback,
            RepurchaseIntent repurchaseIntent) {
        public DrinkRequest { toppings = toppings == null ? List.of() : List.copyOf(toppings); }
    }

    public record DeerRequest(@NotNull Instant occurredAt, BodyFeeling bodyFeeling) {}

    public record RecordView(
            long id, Dimension type, Instant occurredAt, String title,
            Long categoryId, Long brandId, String brandName, String brandShortName,
            String brandColor, String brandLogoUrl, Long itemId, String thumbnailUrl,
            Integer actualAmountFen, Integer rating, RecordSource source,
            String feedback, List<String> memories, Integer ordinalOfDay, String deerMessage) {}
}
