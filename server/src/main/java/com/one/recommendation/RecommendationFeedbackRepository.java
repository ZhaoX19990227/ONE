package com.one.recommendation;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
    List<RecommendationFeedback> findTop100ByUserIdAndDimensionAndExpiresAtAfterOrderByCreatedAtDesc(
            long userId, Dimension dimension, Instant now);
}
