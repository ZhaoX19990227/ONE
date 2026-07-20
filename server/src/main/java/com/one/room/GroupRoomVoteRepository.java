package com.one.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRoomVoteRepository extends JpaRepository<GroupRoomVote, Long> {
    List<GroupRoomVote> findByRoomId(long roomId);
    Optional<GroupRoomVote> findByRoomIdAndUserId(long roomId, long userId);
}
