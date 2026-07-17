package com.one.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "brand_alias")
public class BrandAlias {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "brand_id") private CatalogBrand brand;
    @Column(name = "alias_name", nullable = false, length = 80) private String aliasName;
    protected BrandAlias() {}
    public CatalogBrand getBrand() { return brand; }
    public String getAliasName() { return aliasName; }
}
