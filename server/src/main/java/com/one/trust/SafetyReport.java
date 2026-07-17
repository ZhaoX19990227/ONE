package com.one.trust;

import com.one.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "safety_report")
public class SafetyReport extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 24)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason_code", nullable = false, length = 32)
    private String reasonCode;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "handled_by")
    private Long handledBy;

    @Column(name = "handled_at")
    private Instant handledAt;

    protected SafetyReport() {
    }

    public static SafetyReport create(long reporterId, TargetType targetType, long targetId,
                                      String reasonCode, String description) {
        SafetyReport report = new SafetyReport();
        report.reporterId = reporterId;
        report.targetType = targetType;
        report.targetId = targetId;
        report.reasonCode = reasonCode;
        report.description = description;
        report.status = ReportStatus.PENDING;
        return report;
    }

    public void resolve(long handlerId) {
        status = ReportStatus.RESOLVED;
        handledBy = handlerId;
        handledAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getReporterId() { return reporterId; }
    public TargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getReasonCode() { return reasonCode; }
    public String getDescription() { return description; }
    public ReportStatus getStatus() { return status; }
    public Long getHandledBy() { return handledBy; }
    public Instant getHandledAt() { return handledAt; }

    public enum TargetType { ACTIVITY, USER }
    public enum ReportStatus { PENDING, REVIEWING, RESOLVED, REJECTED }
}
