package com.one.catalog;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogItemRepository extends JpaRepository<CatalogItem, Long> {
    List<CatalogItem> findByDimensionAndActiveTrue(Dimension dimension);
    List<CatalogItem> findByDimensionAndCategoryIdAndActiveTrue(Dimension dimension, long categoryId);
    List<CatalogItem> findByDimensionAndBrandIdAndActiveTrue(Dimension dimension, long brandId);
}
