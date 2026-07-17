package com.one.activity;

import com.one.enrollment.EnrollmentService;
import com.one.common.PageResponse;
import com.one.security.OnePrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final EnrollmentService enrollmentService;

    public ActivityController(ActivityService activityService, EnrollmentService enrollmentService) {
        this.activityService = activityService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public PageResponse<ActivityDtos.ActivitySummary> discover(
            @RequestParam(required = false) @Size(max = 20) String cityCode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(30) int size) {
        return PageResponse.from(activityService.discover(cityCode, page, size));
    }

    @GetMapping("/{activityId}")
    public ActivityDtos.ActivityDetail detail(@PathVariable long activityId,
                                              @AuthenticationPrincipal OnePrincipal principal) {
        return activityService.detail(activityId, principal);
    }

    @PostMapping
    public ActivityDtos.ActivityDetail create(@AuthenticationPrincipal OnePrincipal principal,
                                              @Valid @RequestBody ActivityDtos.CreateActivityRequest request) {
        return activityService.create(principal.userId(), request);
    }

    @PostMapping("/{activityId}/enrollments")
    public EnrollmentService.EnrollmentView enroll(
            @PathVariable long activityId,
            @AuthenticationPrincipal OnePrincipal principal,
            @Valid @RequestBody(required = false) EnrollRequest request) {
        return enrollmentService.enroll(activityId, principal.userId(), request == null ? null : request.note());
    }

    @DeleteMapping("/{activityId}/enrollments/me")
    public EnrollmentService.EnrollmentView cancel(
            @PathVariable long activityId,
            @AuthenticationPrincipal OnePrincipal principal,
            @RequestParam(required = false) @Size(max = 200) String reason) {
        return enrollmentService.cancel(activityId, principal.userId(), reason);
    }

    @PostMapping("/{activityId}/check-in")
    public EnrollmentService.EnrollmentView checkIn(@PathVariable long activityId,
                                                    @AuthenticationPrincipal OnePrincipal principal) {
        return enrollmentService.checkIn(activityId, principal.userId());
    }

    @PostMapping("/{activityId}/enrollments/{enrollmentId}/decision")
    public EnrollmentService.EnrollmentView review(
            @PathVariable long activityId,
            @PathVariable long enrollmentId,
            @AuthenticationPrincipal OnePrincipal principal,
            @Valid @RequestBody EnrollmentDecisionRequest request) {
        return enrollmentService.review(activityId, enrollmentId, principal.userId(), request.approved());
    }

    public record EnrollRequest(@Size(max = 200) String note) {
    }

    public record EnrollmentDecisionRequest(boolean approved) {
    }
}
