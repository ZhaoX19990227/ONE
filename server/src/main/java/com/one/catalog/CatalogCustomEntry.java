package com.one.catalog;

import com.one.common.AuditedEntity;
import com.one.common.Dimension;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalog_custom_entry")
public class CatalogCustomEntry extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Column(name = "category_id") private Long categoryId;
    @Column(name = "brand_name", length = 80) private String brandName;
    @Column(name = "item_name", nullable = false, length = 100) private String itemName;
    @Column(name = "normalized_brand_id") private Long normalizedBrandId;
    @Column(name = "normalized_item_id") private Long normalizedItemId;
    @Column(nullable = false, length = 20) private String status;
    protected CatalogCustomEntry() {}
    public static CatalogCustomEntry pending(long userId, Dimension dimension, Long categoryId,
                                             String brandName, String itemName) {
        CatalogCustomEntry value = new CatalogCustomEntry(); value.userId = userId; value.dimension = dimension;
        value.categoryId = categoryId; value.brandName = brandName; value.itemName = itemName; value.status = "PENDING";
        return value;
    }
}
