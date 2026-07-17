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
@Table(name = "catalog_brand")
public class CatalogBrand extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Column(nullable = false, length = 60) private String code;
    @Column(nullable = false, length = 80) private String name;
    @Column(name = "short_name", length = 30) private String shortName;
    @Column(name = "logo_url", length = 500) private String logoUrl;
    @Column(name = "brand_color", length = 12) private String brandColor;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Column(nullable = false) private boolean active;
    protected CatalogBrand() {}
    public Long getId() { return id; }
    public Dimension getDimension() { return dimension; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getLogoUrl() { return logoUrl; }
    public String getBrandColor() { return brandColor; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
}
