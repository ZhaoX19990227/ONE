package com.one.recognition;

import com.one.common.Dimension;

import java.util.List;

public interface FoodVisionRecognizer {
    VisionResult recognize(Dimension dimension, String contentType, byte[] image,
                           List<String> knownCategories, List<String> knownBrands);

    record VisionResult(List<VisionCandidate> candidates, double confidence) {
        public VisionResult { candidates = candidates == null ? List.of() : List.copyOf(candidates); }
    }

    record VisionCandidate(String categoryName, String brandName, String itemName,
                           double confidence, Integer estimatedAmountFen, String evidence) {}
}
