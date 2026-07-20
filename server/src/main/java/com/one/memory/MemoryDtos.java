package com.one.memory;

import com.one.common.Dimension;

import java.time.Instant;

public final class MemoryDtos {
    private MemoryDtos() {}

    public record View(long id, Dimension dimension, String signal, String displayText,
                       String suggestedValue, Instant sourceAt, long sourceRecordId) {
        static View from(PreferenceMemory memory) {
            return new View(memory.getId(), memory.getDimension(), memory.getSignal().name(),
                    memory.getDisplayText(), memory.getSuggestedValue(), memory.getSourceAt(),
                    memory.getSourceRecordId());
        }
    }
}
