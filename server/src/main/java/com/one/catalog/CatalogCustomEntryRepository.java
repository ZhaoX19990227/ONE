package com.one.catalog;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogCustomEntryRepository extends JpaRepository<CatalogCustomEntry, Long> {
    List<CatalogCustomEntry> findTop100ByStatusOrderByCreatedAtAsc(String status);
    List<CatalogCustomEntry> findByDimensionAndBrandNameAndItemNameAndStatus(
            Dimension dimension, String brandName, String itemName, String status);

    Optional<CatalogCustomEntry> findFirstByDimensionAndBrandNameAndItemNameAndStatusAndNormalizedItemIdNotNullOrderByUpdatedAtDesc(
            Dimension dimension, String brandName, String itemName, String status);
}
