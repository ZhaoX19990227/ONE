package com.one.enrollment;

import com.one.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "enrollment")
public class Enrollment extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EnrollmentStatus status;

    @Column(name = "apply_note", length = 200)
    private String applyNote;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Version
    private Long version;

    protected Enrollment() {
    }

    public static Enrollment create(long activityId, long userId, EnrollmentStatus status, String note) {
        Enrollment enrollment = new Enrollment();
        enrollment.activityId = activityId;
        enrollment.userId = userId;
        enrollment.status = status;
        enrollment.applyNote = note;
        return enrollment;
    }

    public boolean occupiesSeat() {
        return status == EnrollmentStatus.CONFIRMED
                || status == EnrollmentStatus.CHECKED_IN
                || status == EnrollmentStatus.COMPLETED;
    }

    public void cancel(String reason) {
        status = EnrollmentStatus.CANCELLED;
        cancelReason = reason;
    }

    public void confirm() {
        if (status != EnrollmentStatus.APPLIED && status != EnrollmentStatus.WAITLISTED) {
            throw new IllegalStateException("Only applied or waitlisted enrollment can be confirmed");
        }
        status = EnrollmentStatus.CONFIRMED;
    }

    public void waitlist() {
        if (status != EnrollmentStatus.APPLIED) {
            throw new IllegalStateException("Only applied enrollment can be waitlisted");
        }
        status = EnrollmentStatus.WAITLISTED;
    }

    public void reject() {
        if (status != EnrollmentStatus.APPLIED) {
            throw new IllegalStateException("Only applied enrollment can be rejected");
        }
        status = EnrollmentStatus.REJECTED;
    }

    public void checkIn(Instant at) {
        if (status != EnrollmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed enrollment can check in");
        }
        status = EnrollmentStatus.CHECKED_IN;
        checkedInAt = at;
    }

    public Long getId() { return id; }
    public Long getActivityId() { return activityId; }
    public Long getUserId() { return userId; }
    public EnrollmentStatus getStatus() { return status; }
    public String getApplyNote() { return applyNote; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCheckedInAt() { return checkedInAt; }
}
