package com.one.room;

import com.one.common.Dimension;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;

public final class GroupRoomDtos {
    private GroupRoomDtos() {}
    public record CreateRequest(@Size(max = 60) String title, @NotNull Dimension dimension,
                                @NotEmpty @Size(min = 2, max = 8) List<Long> itemIds) {}
    public record VoteRequest(@Positive long candidateId) {}
    public record Candidate(long id, long itemId, String itemName, String brandName, String brandShortName,
                            String brandColor, Integer defaultPriceFen, int votes, boolean myVote, boolean winner) {}
    public record View(String shareCode, String title, Dimension dimension, GroupRoomStatus status,
                       String ownerNickname, boolean owner, int totalVotes, Instant expiresAt,
                       Long winnerCandidateId, List<Candidate> candidates) {}
}
