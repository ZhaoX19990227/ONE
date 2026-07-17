package com.one.recommendation;

import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogCategory;
import com.one.catalog.CatalogItem;
import com.one.memory.PreferenceMemory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "decision_candidate")
public class DecisionCandidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id") private DecisionSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id") private CatalogCategory category;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "brand_id") private CatalogBrand brand;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id") private CatalogItem item;
    @Column(nullable = false) private int score;
    @Column(name = "reason_text", nullable = false, length = 500) private String reasonText;
    @Column(name = "suggestion_json", columnDefinition = "json") private String suggestionJson;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "memory_id") private PreferenceMemory memory;
    @Column(name = "position_no", nullable = false) private int positionNo;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected DecisionCandidate() {}

    public static DecisionCandidate of(DecisionSession session, CatalogItem item, int score,
                                       String reason, String suggestionJson,
                                       PreferenceMemory memory, int position) {
        DecisionCandidate candidate = new DecisionCandidate();
        candidate.session = session; candidate.category = item.getCategory(); candidate.brand = item.getBrand();
        candidate.item = item; candidate.score = score; candidate.reasonText = reason;
        candidate.suggestionJson = suggestionJson; candidate.memory = memory; candidate.positionNo = position;
        return candidate;
    }

    @PrePersist void beforeCreate() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public DecisionSession getSession() { return session; }
    public CatalogCategory getCategory() { return category; }
    public CatalogBrand getBrand() { return brand; }
    public CatalogItem getItem() { return item; }
    public int getScore() { return score; }
    public String getReasonText() { return reasonText; }
    public String getSuggestionJson() { return suggestionJson; }
    public PreferenceMemory getMemory() { return memory; }
    public int getPositionNo() { return positionNo; }
}
