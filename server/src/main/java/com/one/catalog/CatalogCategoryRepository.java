package com.one.catalog;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogCategoryRepository extends JpaRepository<CatalogCategory, Long> {
    List<CatalogCategory> findByDimensionAndActiveTrueOrderBySortOrderAsc(Dimension dimension);
}
