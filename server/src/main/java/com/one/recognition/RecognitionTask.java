package com.one.recognition;

import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogCategory;
import com.one.catalog.CatalogItem;
import com.one.common.AuditedEntity;
import com.one.common.Dimension;
import com.one.media.MediaAsset;
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

import java.math.BigDecimal;

@Entity
@Table(name = "recognition_task")
public class RecognitionTask extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "media_asset_id") private MediaAsset mediaAsset;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private RecognitionStatus status;
    @Column(nullable = false, length = 40) private String provider;
    @Column(name = "candidates_json", columnDefinition = "json") private String candidatesJson;
    @Column(precision = 5, scale = 4) private BigDecimal confidence;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "confirmed_category_id") private CatalogCategory confirmedCategory;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "confirmed_brand_id") private CatalogBrand confirmedBrand;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "confirmed_item_id") private CatalogItem confirmedItem;
    @Column(name = "custom_brand_name", length = 80) private String customBrandName;
    @Column(name = "custom_item_name", length = 100) private String customItemName;
    @Column(name = "failure_code", length = 40) private String failureCode;

    protected RecognitionTask() {}

    public static RecognitionTask processing(long userId, MediaAsset media, Dimension dimension, String provider) {
        RecognitionTask task = new RecognitionTask();
        task.userId = userId;
        task.mediaAsset = media;
        task.dimension = dimension;
        task.status = RecognitionStatus.PROCESSING;
        task.provider = provider;
        return task;
    }

    public void needConfirmation(String candidatesJson, BigDecimal confidence) {
        this.candidatesJson = candidatesJson;
        this.confidence = confidence;
        this.status = RecognitionStatus.NEED_CONFIRMATION;
        this.failureCode = null;
    }

    public void failed(String failureCode) {
        this.status = RecognitionStatus.FAILED;
        this.failureCode = failureCode;
    }

    public void confirm(CatalogCategory category, CatalogBrand brand, CatalogItem item,
                        String customBrandName, String customItemName) {
        this.confirmedCategory = category;
        this.confirmedBrand = brand;
        this.confirmedItem = item;
        this.customBrandName = customBrandName;
        this.customItemName = customItemName;
        this.status = RecognitionStatus.CONFIRMED;
    }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public MediaAsset getMediaAsset() { return mediaAsset; }
    public Dimension getDimension() { return dimension; }
    public RecognitionStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getCandidatesJson() { return candidatesJson; }
    public BigDecimal getConfidence() { return confidence; }
    public CatalogCategory getConfirmedCategory() { return confirmedCategory; }
    public CatalogBrand getConfirmedBrand() { return confirmedBrand; }
    public CatalogItem getConfirmedItem() { return confirmedItem; }
    public String getCustomBrandName() { return customBrandName; }
    public String getCustomItemName() { return customItemName; }
    public String getFailureCode() { return failureCode; }
}
