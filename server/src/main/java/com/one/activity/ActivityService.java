package com.one.activity;

import com.one.common.BusinessException;
import com.one.enrollment.Enrollment;
import com.one.enrollment.EnrollmentRepository;
import com.one.integration.safety.ContentSafetyGateway;
import com.one.security.OnePrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.EnumSet;

@Service
public class ActivityService {

    private static final EnumSet<ActivityStatus> DISCOVERABLE =
            EnumSet.of(ActivityStatus.PUBLISHED, ActivityStatus.FULL);

    private final ActivityRepository activityRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ObjectMapper objectMapper;
    private final ContentSafetyGateway contentSafetyGateway;

    public ActivityService(ActivityRepository activityRepository, EnrollmentRepository enrollmentRepository,
                           ObjectMapper objectMapper, ContentSafetyGateway contentSafetyGateway) {
        this.activityRepository = activityRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.objectMapper = objectMapper;
        this.contentSafetyGateway = contentSafetyGateway;
    }

    @Transactional(readOnly = true)
    public Page<ActivityDtos.ActivitySummary> discover(String cityCode, int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 30), Sort.by("startAt").ascending());
        Page<Activity> activities = cityCode == null || cityCode.isBlank()
                ? activityRepository.findByStatusInAndStartAtAfter(DISCOVERABLE, Instant.now(), pageable)
                : activityRepository.findByCityCodeAndStatusInAndStartAtAfter(
                        cityCode, DISCOVERABLE, Instant.now(), pageable);
        return activities.map(ActivityDtos.ActivitySummary::from);
    }

    @Transactional(readOnly = true)
    public ActivityDtos.ActivityDetail detail(long activityId, OnePrincipal principal) {
        Activity activity = findActivity(activityId);
        boolean reveal = principal != null && (activity.getOwnerId() == principal.userId()
                || enrollmentRepository.findByActivityIdAndUserId(activityId, principal.userId())
                .filter(Enrollment::occupiesSeat).isPresent());
        return ActivityDtos.ActivityDetail.from(activity, reveal);
    }

    @Transactional
    public ActivityDtos.ActivityDetail create(long ownerId, ActivityDtos.CreateActivityRequest request) {
        validateSchedule(request);
        ContentSafetyGateway.SafetyResult safety = contentSafetyGateway.check(
                request.title() + "\n" + request.description());
        if (!safety.safe()) {
            throw new BusinessException("CONTENT_REQUIRES_REVIEW", "活动内容需要调整后再发布", HttpStatus.BAD_REQUEST);
        }
        try {
            Activity activity = Activity.create(new Activity.ActivityDraft(
                    ownerId, request.type(), request.mode(), request.title().strip(), request.description().strip(),
                    request.coverUrl(), request.cityCode(), request.cityName(), request.district(), request.address(),
                    request.latitude(), request.longitude(), request.startAt(), request.endAt(),
                    request.enrollDeadline(), request.capacity(), request.feeFen(), request.depositFen(),
                    request.approvalRequired(), objectMapper.writeValueAsString(request.tags()),
                    objectMapper.writeValueAsString(request.attributes())));
            return ActivityDtos.ActivityDetail.from(activityRepository.save(activity), true);
        } catch (JacksonException exception) {
            throw new BusinessException("INVALID_ACTIVITY_ATTRIBUTES", "活动扩展信息格式不正确", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateSchedule(ActivityDtos.CreateActivityRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessException("INVALID_ACTIVITY_TIME", "结束时间必须晚于开始时间", HttpStatus.BAD_REQUEST);
        }
        if (request.enrollDeadline().isAfter(request.startAt())) {
            throw new BusinessException("INVALID_ENROLL_DEADLINE", "报名截止时间不能晚于活动开始时间", HttpStatus.BAD_REQUEST);
        }
        if (request.mode() == ActivityMode.OFFLINE
                && (request.cityCode() == null || request.cityCode().isBlank())) {
            throw new BusinessException("CITY_REQUIRED", "线下活动需要选择城市", HttpStatus.BAD_REQUEST);
        }
    }

    private Activity findActivity(long activityId) {
        return activityRepository.findById(activityId).orElseThrow(() ->
                new BusinessException("ACTIVITY_NOT_FOUND", "活动不存在或已下架", HttpStatus.NOT_FOUND));
    }
}
