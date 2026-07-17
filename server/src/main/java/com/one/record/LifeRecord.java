package com.one.record;

import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogCategory;
import com.one.catalog.CatalogItem;
import com.one.common.AuditedEntity;
import com.one.common.Dimension;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "life_record")
public class LifeRecord extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @Enumerated(EnumType.STRING) @Column(name = "record_type", nullable = false, length = 20) private Dimension recordType;
    @Enumerated(EnumType.STRING) @Column(name = "record_status", nullable = false, length = 20) private RecordStatus recordStatus;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "decision_session_id") private Long decisionSessionId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") private CatalogCategory category;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "brand_id") private CatalogBrand brand;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id") private CatalogItem item;
    @Column(nullable = false, length = 120) private String title;
    @Column(name = "brand_name_snapshot", length = 80) private String brandNameSnapshot;
    @Column(name = "item_name_snapshot", length = 100) private String itemNameSnapshot;
    @Enumerated(EnumType.STRING) @Column(name = "catalog_match_status", nullable = false, length = 20) private CatalogMatchStatus catalogMatchStatus;
    @Column(name = "thumbnail_url", length = 500) private String thumbnailUrl;
    @Column(name = "original_amount_fen") private Integer originalAmountFen;
    @Column(name = "discount_amount_fen") private Integer discountAmountFen;
    @Column(name = "actual_amount_fen") private Integer actualAmountFen;
    private Integer rating;
    @Column(length = 300) private String note;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RecordSource source;
    @Version private long version;

    protected LifeRecord() {}

    public static LifeRecord confirmed(long userId, Dimension type, Instant occurredAt, Long decisionSessionId,
                                       CatalogCategory category, CatalogBrand brand, CatalogItem item,
                                       String customBrandName, String customTitle, String thumbnailUrl, MoneyInput money, Integer rating,
                                       String note, RecordSource source) {
        LifeRecord record = new LifeRecord();
        record.userId = userId;
        record.recordType = type;
        record.recordStatus = RecordStatus.CONFIRMED;
        record.occurredAt = occurredAt;
        record.decisionSessionId = decisionSessionId;
        record.category = category;
        record.brand = brand;
        record.item = item;
        record.brandNameSnapshot = brand == null ? customBrandName : brand.getName();
        record.itemNameSnapshot = item == null ? customTitle : item.getName();
        record.title = item != null ? item.getName() : customTitle;
        record.catalogMatchStatus = item != null ? CatalogMatchStatus.MATCHED : CatalogMatchStatus.CUSTOM;
        record.thumbnailUrl = thumbnailUrl;
        record.originalAmountFen = money == null ? null : money.originalAmountFen();
        record.discountAmountFen = money == null ? null : money.discountAmountFen();
        record.actualAmountFen = money == null ? null : money.actualAmountFen();
        record.rating = rating;
        record.note = note;
        record.source = source;
        return record;
    }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public Dimension getRecordType() { return recordType; }
    public RecordStatus getRecordStatus() { return recordStatus; }
    public Instant getOccurredAt() { return occurredAt; }
    public Long getDecisionSessionId() { return decisionSessionId; }
    public CatalogCategory getCategory() { return category; }
    public CatalogBrand getBrand() { return brand; }
    public CatalogItem getItem() { return item; }
    public String getTitle() { return title; }
    public String getBrandNameSnapshot() { return brandNameSnapshot; }
    public String getItemNameSnapshot() { return itemNameSnapshot; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getOriginalAmountFen() { return originalAmountFen; }
    public Integer getDiscountAmountFen() { return discountAmountFen; }
    public Integer getActualAmountFen() { return actualAmountFen; }
    public Integer getRating() { return rating; }
    public String getNote() { return note; }
    public RecordSource getSource() { return source; }

    public record MoneyInput(Integer originalAmountFen, Integer discountAmountFen, Integer actualAmountFen) {}
}
