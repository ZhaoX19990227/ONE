package com.one.admin;

import com.one.activity.Activity;
import com.one.activity.ActivityRepository;
import com.one.activity.ActivityStatus;
import com.one.common.PageResponse;
import com.one.security.OnePrincipal;
import com.one.trust.SafetyReport;
import com.one.trust.SafetyReportRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ActivityRepository activityRepository;
    private final SafetyReportRepository reportRepository;

    public AdminController(ActivityRepository activityRepository, SafetyReportRepository reportRepository) {
        this.activityRepository = activityRepository;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/overview")
    public Overview overview() {
        long totalActivities = activityRepository.count();
        long pendingReports = reportRepository.findByStatus(
                SafetyReport.ReportStatus.PENDING, PageRequest.of(0, 1)).getTotalElements();
        return new Overview(totalActivities, pendingReports, Instant.now());
    }

    @GetMapping("/activities")
    public PageResponse<ActivityRow> activities(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return PageResponse.from(activityRepository
                .findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(ActivityRow::from));
    }

    @GetMapping("/reports")
    public PageResponse<ReportRow> reports(@RequestParam(defaultValue = "PENDING") SafetyReport.ReportStatus status,
                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                           @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return PageResponse.from(reportRepository.findByStatus(status,
                        PageRequest.of(page, size, Sort.by("createdAt").ascending()))
                .map(ReportRow::from));
    }

    @PostMapping("/reports/{reportId}/resolve")
    @Transactional
    public ReportRow resolve(@PathVariable long reportId, @AuthenticationPrincipal OnePrincipal principal) {
        SafetyReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "举报不存在"));
        report.resolve(principal.userId());
        return ReportRow.from(report);
    }

    public record Overview(long totalActivities, long pendingReports, Instant generatedAt) {
    }

    public record ActivityRow(long id, String title, String type, String cityName, Instant startAt,
                              int joinedCount, int capacity, ActivityStatus status) {
        static ActivityRow from(Activity activity) {
            return new ActivityRow(activity.getId(), activity.getTitle(), activity.getType().name(),
                    activity.getCityName(), activity.getStartAt(), activity.getJoinedCount(),
                    activity.getCapacity(), activity.getStatus());
        }
    }

    public record ReportRow(long id, String targetType, long targetId, String reasonCode,
                            String description, String status, Instant createdAt) {
        static ReportRow from(SafetyReport report) {
            return new ReportRow(report.getId(), report.getTargetType().name(), report.getTargetId(),
                    report.getReasonCode(), report.getDescription(), report.getStatus().name(), report.getCreatedAt());
        }
    }
}
