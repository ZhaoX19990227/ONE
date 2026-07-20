package com.one.recommendation;

import com.one.common.Dimension;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "recommendation_feedback")
public class RecommendationFeedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Column(name = "item_id", nullable = false) private long itemId;
    @Column(name = "source_session_id") private Long sourceSessionId;
    @Column(name = "feedback_type", nullable = false, length = 20) private String feedbackType;
    @Column(length = 40) private String reason;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected RecommendationFeedback() {}

    public static RecommendationFeedback dismissed(long userId, Dimension dimension, long itemId,
                                                     long sessionId, String reason) {
        RecommendationFeedback value = new RecommendationFeedback();
        value.userId = userId; value.dimension = dimension; value.itemId = itemId;
        value.sourceSessionId = sessionId; value.feedbackType = "DISMISS";
        value.reason = reason == null || reason.isBlank() ? null : reason.strip();
        value.expiresAt = Instant.now().plus(Duration.ofDays(14));
        return value;
    }

    @PrePersist void beforeCreate() { createdAt = Instant.now(); }
    public long getItemId() { return itemId; }
}
