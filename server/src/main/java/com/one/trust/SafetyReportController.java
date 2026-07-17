package com.one.trust;

import com.one.security.OnePrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class SafetyReportController {

    private final SafetyReportRepository reportRepository;

    public SafetyReportController(SafetyReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @PostMapping
    public ReportView create(@AuthenticationPrincipal OnePrincipal principal,
                             @Valid @RequestBody CreateReportRequest request) {
        SafetyReport report = reportRepository.save(SafetyReport.create(
                principal.userId(), request.targetType(), request.targetId(),
                request.reasonCode(), request.description()));
        return ReportView.from(report);
    }

    public record CreateReportRequest(
            @NotNull SafetyReport.TargetType targetType,
            @Positive long targetId,
            @NotBlank @Size(max = 32) String reasonCode,
            @Size(max = 500) String description
    ) {
    }

    public record ReportView(long id, String status) {
        static ReportView from(SafetyReport report) {
            return new ReportView(report.getId(), report.getStatus().name());
        }
    }
}
