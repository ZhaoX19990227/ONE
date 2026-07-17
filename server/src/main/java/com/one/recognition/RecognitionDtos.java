package com.one.recognition;

import com.one.common.Dimension;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class RecognitionDtos {
    private RecognitionDtos() {}

    public record StartRequest(@NotNull Long mediaAssetId, @NotNull Dimension dimension) {}

    public record ConfirmRequest(Long categoryId, Long brandId, Long itemId,
                                 @Size(max = 80) String customBrandName,
                                 @Size(max = 100) String customItemName) {}

    public record Candidate(Long categoryId, String categoryName,
                            Long brandId, String brandName, String brandLogoUrl,
                            Long itemId, String itemName, double confidence,
                            Integer estimatedAmountFen, String evidence,
                            boolean catalogMatched) {}

    public record View(long id, long mediaAssetId, String imageUrl, Dimension dimension,
                       RecognitionStatus status, String provider, double confidence,
                       List<Candidate> candidates, String failureCode,
                       String fallbackMessage) {}
}
