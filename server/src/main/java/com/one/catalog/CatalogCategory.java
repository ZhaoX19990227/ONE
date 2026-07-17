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
@Table(name = "catalog_category")
public class CatalogCategory extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Column(nullable = false, length = 40) private String code;
    @Column(nullable = false, length = 40) private String name;
    @Column(length = 32) private String icon;
    @Column(length = 12) private String color;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Column(nullable = false) private boolean active;
    protected CatalogCategory() {}
    public Long getId() { return id; }
    public Dimension getDimension() { return dimension; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
    public int getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
}
