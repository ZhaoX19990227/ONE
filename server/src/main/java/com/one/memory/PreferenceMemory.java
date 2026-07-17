package com.one.memory;

import com.one.common.AuditedEntity;
import com.one.common.Dimension;
import com.one.record.LifeRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "preference_memory")
public class PreferenceMemory extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Column(name = "category_id") private Long categoryId;
    @Column(name = "brand_id") private Long brandId;
    @Column(name = "item_id") private Long itemId;
    @Enumerated(EnumType.STRING) @Column(name = "memory_signal", nullable = false, length = 30) private MemorySignal signal;
    @Column(name = "attribute_key", length = 40) private String attributeKey;
    @Column(name = "observed_value", length = 80) private String observedValue;
    @Column(name = "suggested_value", length = 80) private String suggestedValue;
    @Column(name = "display_text", nullable = false, length = 300) private String displayText;
    @Column(name = "source_record_id", nullable = false) private long sourceRecordId;
    @Column(name = "source_at", nullable = false) private Instant sourceAt;
    @Column(nullable = false) private int strength;
    @Column(nullable = false) private boolean active;
    protected PreferenceMemory() {}

    public static PreferenceMemory from(LifeRecord record, MemorySignal signal, String attributeKey,
                                        String observedValue, String suggestedValue, String displayText, int strength) {
        PreferenceMemory memory = new PreferenceMemory();
        memory.userId = record.getUserId(); memory.dimension = record.getRecordType();
        memory.categoryId = record.getCategory() == null ? null : record.getCategory().getId();
        memory.brandId = record.getBrand() == null ? null : record.getBrand().getId();
        memory.itemId = record.getItem() == null ? null : record.getItem().getId();
        memory.signal = signal; memory.attributeKey = attributeKey; memory.observedValue = observedValue;
        memory.suggestedValue = suggestedValue; memory.displayText = displayText;
        memory.sourceRecordId = record.getId(); memory.sourceAt = record.getOccurredAt();
        memory.strength = strength; memory.active = true;
        return memory;
    }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public Dimension getDimension() { return dimension; }
    public Long getCategoryId() { return categoryId; }
    public Long getBrandId() { return brandId; }
    public Long getItemId() { return itemId; }
    public MemorySignal getSignal() { return signal; }
    public String getAttributeKey() { return attributeKey; }
    public String getObservedValue() { return observedValue; }
    public String getSuggestedValue() { return suggestedValue; }
    public String getDisplayText() { return displayText; }
    public Instant getSourceAt() { return sourceAt; }
    public int getStrength() { return strength; }
}
