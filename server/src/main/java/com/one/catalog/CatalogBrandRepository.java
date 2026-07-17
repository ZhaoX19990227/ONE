package com.one.catalog;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogBrandRepository extends JpaRepository<CatalogBrand, Long> {
    List<CatalogBrand> findByDimensionAndActiveTrueOrderBySortOrderAsc(Dimension dimension);
}
