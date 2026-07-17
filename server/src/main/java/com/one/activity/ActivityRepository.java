package com.one.activity;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Activity a where a.id = :id")
    Optional<Activity> findByIdForUpdate(@Param("id") long id);

    Page<Activity> findByStatusInAndStartAtAfter(
            Collection<ActivityStatus> statuses, Instant startAt, Pageable pageable);

    Page<Activity> findByCityCodeAndStatusInAndStartAtAfter(
            String cityCode, Collection<ActivityStatus> statuses, Instant startAt, Pageable pageable);
}
