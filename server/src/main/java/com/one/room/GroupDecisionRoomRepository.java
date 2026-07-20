package com.one.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface GroupDecisionRoomRepository extends JpaRepository<GroupDecisionRoom, Long> {
    Optional<GroupDecisionRoom> findByShareCode(String shareCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select room from GroupDecisionRoom room where room.shareCode = :shareCode")
    Optional<GroupDecisionRoom> findByShareCodeForUpdate(String shareCode);
    boolean existsByShareCode(String shareCode);
}
