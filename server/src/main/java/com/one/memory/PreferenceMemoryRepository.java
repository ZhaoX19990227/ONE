package com.one.memory;

import com.one.common.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreferenceMemoryRepository extends JpaRepository<PreferenceMemory, Long> {
    List<PreferenceMemory> findTop100ByUserIdAndDimensionAndActiveTrueOrderBySourceAtDesc(
            long userId, Dimension dimension);
    List<PreferenceMemory> findTop100ByUserIdAndActiveTrueOrderBySourceAtDesc(long userId);
    List<PreferenceMemory> findByUserIdAndSourceRecordIdAndActiveTrue(long userId, long sourceRecordId);
    Optional<PreferenceMemory> findByIdAndUserIdAndActiveTrue(long id, long userId);
    List<PreferenceMemory> findTop10ByUserIdAndDimensionAndItemIdAndActiveTrueOrderBySourceAtDesc(
            long userId, Dimension dimension, long itemId);
    List<PreferenceMemory> findTop10ByUserIdAndDimensionAndBrandIdAndActiveTrueOrderBySourceAtDesc(
            long userId, Dimension dimension, long brandId);
}
