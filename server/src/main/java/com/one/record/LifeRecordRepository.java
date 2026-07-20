package com.one.record;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.one.catalog.CatalogItem;
import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogCategory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LifeRecordRepository extends JpaRepository<LifeRecord, Long> {
    List<LifeRecord> findByUserIdAndRecordStatusAndOccurredAtBetweenOrderByOccurredAtAsc(
            long userId, RecordStatus status, Instant from, Instant to);
    long countByUserIdAndRecordTypeAndRecordStatusAndOccurredAtBetween(
            long userId, Dimension type, RecordStatus status, Instant from, Instant to);
    Optional<LifeRecord> findByIdAndUserIdAndRecordStatus(long id, long userId, RecordStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update LifeRecord record set record.category = :targetCategory, record.brand = :targetBrand,
              record.item = :target, record.title = :targetName, record.itemNameSnapshot = :targetName,
              record.brandNameSnapshot = :targetBrandName, record.catalogMatchStatus = com.one.record.CatalogMatchStatus.MATCHED
            where record.recordType = :dimension and record.item is null and record.itemNameSnapshot = :itemName
              and ((:brandName is null and record.brandNameSnapshot is null) or record.brandNameSnapshot = :brandName)
            """)
    int normalizeCustomRecords(Dimension dimension, String brandName, String itemName,
                               CatalogCategory targetCategory, CatalogBrand targetBrand,
                               CatalogItem target, String targetName, String targetBrandName);
}
