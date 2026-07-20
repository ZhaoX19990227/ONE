package com.one.catalog;

import com.one.common.Dimension;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class CatalogAdminDtos {
    private CatalogAdminDtos() {}
    public record Pending(long id, Dimension dimension, Long categoryId, String brandName, String itemName) {}
    public record NormalizeRequest(@NotNull Long targetItemId) {}
    public record NormalizeResult(long sourceId, long targetItemId, int mergedEntries,
                                  int normalizedHistoricalRecords, boolean aliasCreated) {}
    public record PendingList(List<Pending> entries) {}
}
