package com.one.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByActivityIdAndUserId(long activityId, long userId);
    List<Enrollment> findByUserIdOrderByCreatedAtDesc(long userId);
    List<Enrollment> findByActivityIdOrderByCreatedAtAsc(long activityId);
    Optional<Enrollment> findFirstByActivityIdAndStatusOrderByCreatedAtAsc(
            long activityId, EnrollmentStatus status);
}
