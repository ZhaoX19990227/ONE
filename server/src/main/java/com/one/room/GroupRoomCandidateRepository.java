package com.one.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRoomCandidateRepository extends JpaRepository<GroupRoomCandidate, Long> {
    List<GroupRoomCandidate> findByRoomIdOrderByPositionNoAsc(long roomId);
    Optional<GroupRoomCandidate> findByIdAndRoomId(long id, long roomId);
}
