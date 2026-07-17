package com.one.catalog;

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

@Entity
@Table(name = "catalog_item")
public class CatalogItem extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id") private CatalogCategory category;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "brand_id") private CatalogBrand brand;
    @Column(nullable = false, length = 80) private String code;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "image_url", length = 500) private String imageUrl;
    @Column(name = "default_price_fen") private Integer defaultPriceFen;
    @Column(columnDefinition = "json") private String attributes;
    @Column(name = "base_weight", nullable = false) private int baseWeight;
    @Column(nullable = false) private boolean active;
    protected CatalogItem() {}
    public Long getId() { return id; }
    public Dimension getDimension() { return dimension; }
    public CatalogCategory getCategory() { return category; }
    public CatalogBrand getBrand() { return brand; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public Integer getDefaultPriceFen() { return defaultPriceFen; }
    public String getAttributes() { return attributes; }
    public int getBaseWeight() { return baseWeight; }
    public boolean isActive() { return active; }
}
