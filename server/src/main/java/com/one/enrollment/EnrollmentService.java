package com.one.enrollment;

import com.one.activity.Activity;
import com.one.activity.ActivityRepository;
import com.one.common.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class EnrollmentService {

    private final ActivityRepository activityRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(ActivityRepository activityRepository, EnrollmentRepository enrollmentRepository) {
        this.activityRepository = activityRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public EnrollmentView enroll(long activityId, long userId, String note) {
        Activity activity = activityRepository.findByIdForUpdate(activityId).orElseThrow(() ->
                new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在或已下架", HttpStatus.NOT_FOUND));
        if (activity.getOwnerId() == userId) {
            throw new BusinessException("OWNER_CANNOT_ENROLL", "发起人已经在活动中", HttpStatus.CONFLICT);
        }
        Enrollment existing = enrollmentRepository.findByActivityIdAndUserId(activityId, userId).orElse(null);
        if (existing != null) {
            return EnrollmentView.from(existing);
        }
        if (!activity.isEnrollmentOpen(Instant.now())) {
            throw new BusinessException("ENROLLMENT_CLOSED", "当前活动已停止报名", HttpStatus.CONFLICT);
        }

        EnrollmentStatus status;
        if (!activity.hasAvailableSeat()) {
            status = EnrollmentStatus.WAITLISTED;
        } else if (activity.isApprovalRequired()) {
            status = EnrollmentStatus.APPLIED;
        } else {
            status = EnrollmentStatus.CONFIRMED;
            activity.occupySeat();
        }

        try {
            return EnrollmentView.from(enrollmentRepository.save(
                    Enrollment.create(activityId, userId, status, normalizeNote(note))));
        } catch (DataIntegrityViolationException exception) {
            return enrollmentRepository.findByActivityIdAndUserId(activityId, userId)
                    .map(EnrollmentView::from)
                    .orElseThrow(() -> exception);
        }
    }

    @Transactional
    public EnrollmentView cancel(long activityId, long userId, String reason) {
        Activity activity = activityRepository.findByIdForUpdate(activityId).orElseThrow(() ->
                new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在", HttpStatus.NOT_FOUND));
        Enrollment enrollment = enrollmentRepository.findByActivityIdAndUserId(activityId, userId).orElseThrow(() ->
                new BusinessException("ENROLLMENT_NOT_FOUND", "尚未报名该活动", HttpStatus.NOT_FOUND));
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return EnrollmentView.from(enrollment);
        }
        if (enrollment.occupiesSeat()) {
            activity.releaseSeat();
        }
        enrollment.cancel(reason == null ? null : reason.strip());
        promoteWaitlistedIfPossible(activity);
        return EnrollmentView.from(enrollment);
    }

    @Transactional
    public EnrollmentView review(long activityId, long enrollmentId, long ownerId, boolean approved) {
        Activity activity = activityRepository.findByIdForUpdate(activityId).orElseThrow(() ->
                new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在", HttpStatus.NOT_FOUND));
        if (activity.getOwnerId() != ownerId) {
            throw new BusinessException("NOT_ACTIVITY_OWNER", "只有发起人可以处理报名", HttpStatus.FORBIDDEN);
        }
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .filter(item -> item.getActivityId() == activityId)
                .orElseThrow(() -> new BusinessException(
                        "ENROLLMENT_NOT_FOUND", "报名记录不存在", HttpStatus.NOT_FOUND));
        if (enrollment.getStatus() != EnrollmentStatus.APPLIED) {
            return EnrollmentView.from(enrollment);
        }
        if (!approved) {
            enrollment.reject();
        } else if (activity.isEnrollmentOpen(Instant.now()) && activity.hasAvailableSeat()) {
            enrollment.confirm();
            activity.occupySeat();
        } else {
            enrollment.waitlist();
        }
        return EnrollmentView.from(enrollment);
    }

    @Transactional
    public EnrollmentView checkIn(long activityId, long userId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() ->
                new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在", HttpStatus.NOT_FOUND));
        Instant now = Instant.now();
        if (now.isBefore(activity.getStartAt().minusSeconds(2 * 60 * 60)) || now.isAfter(activity.getEndAt())) {
            throw new BusinessException("OUTSIDE_CHECK_IN_WINDOW", "请在活动开始前两小时至结束前签到", HttpStatus.CONFLICT);
        }
        Enrollment enrollment = enrollmentRepository.findByActivityIdAndUserId(activityId, userId).orElseThrow(() ->
                new BusinessException("ENROLLMENT_NOT_FOUND", "尚未报名该活动", HttpStatus.NOT_FOUND));
        try {
            enrollment.checkIn(now);
        } catch (IllegalStateException exception) {
            throw new BusinessException("CHECK_IN_NOT_ALLOWED", "当前状态暂时无法签到", HttpStatus.CONFLICT);
        }
        return EnrollmentView.from(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentView> myEnrollments(long userId) {
        return enrollmentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(EnrollmentView::from)
                .toList();
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String value = note.strip();
        return value.length() <= 200 ? value : value.substring(0, 200);
    }

    private void promoteWaitlistedIfPossible(Activity activity) {
        if (!activity.hasAvailableSeat() || !activity.isEnrollmentOpen(Instant.now())) {
            return;
        }
        enrollmentRepository.findFirstByActivityIdAndStatusOrderByCreatedAtAsc(
                activity.getId(), EnrollmentStatus.WAITLISTED).ifPresent(waitlisted -> {
            waitlisted.confirm();
            activity.occupySeat();
        });
    }

    public record EnrollmentView(long id, long activityId, long userId, EnrollmentStatus status,
                                 String applyNote, Instant checkedInAt) {
        static EnrollmentView from(Enrollment enrollment) {
            return new EnrollmentView(enrollment.getId(), enrollment.getActivityId(), enrollment.getUserId(),
                    enrollment.getStatus(), enrollment.getApplyNote(), enrollment.getCheckedInAt());
        }
    }
}
