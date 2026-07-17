package com.one.analytics;

import com.one.security.OnePrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping
public class AnalyticsController {
    private final AnalyticsService service;
    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @GetMapping("/calendar/month")
    public AnalyticsDtos.CalendarMonth month(@AuthenticationPrincipal OnePrincipal principal,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.month(principal.userId(), month);
    }

    @GetMapping("/analytics/summary")
    public AnalyticsDtos.Summary summary(@AuthenticationPrincipal OnePrincipal principal,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.summary(principal.userId(), from, to);
    }
}
