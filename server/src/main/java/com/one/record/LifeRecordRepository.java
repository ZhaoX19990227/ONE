package com.one.record;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface LifeRecordRepository extends JpaRepository<LifeRecord, Long> {
    List<LifeRecord> findByUserIdAndRecordStatusAndOccurredAtBetweenOrderByOccurredAtAsc(
            long userId, RecordStatus status, Instant from, Instant to);
    long countByUserIdAndRecordTypeAndRecordStatusAndOccurredAtBetween(
            long userId, Dimension type, RecordStatus status, Instant from, Instant to);
}
