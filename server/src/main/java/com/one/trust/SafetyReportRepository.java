package com.one.trust;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {
    Page<SafetyReport> findByStatus(SafetyReport.ReportStatus status, Pageable pageable);
}
