package com.one.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DecisionCandidateRepository extends JpaRepository<DecisionCandidate, Long> {
    List<DecisionCandidate> findBySessionIdOrderByPositionNoAsc(long sessionId);
    Optional<DecisionCandidate> findByIdAndSessionId(long id, long sessionId);
}
