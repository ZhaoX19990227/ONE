package com.one.record;

import com.one.security.OnePrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/records")
public class RecordController {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final RecordService records;
    public RecordController(RecordService records) { this.records = records; }

    @PostMapping("/meals")
    public RecordDtos.RecordView meal(@AuthenticationPrincipal OnePrincipal principal,
                                      @Valid @RequestBody RecordDtos.MealRequest request) throws Exception {
        return records.createMeal(principal.userId(), request);
    }

    @PostMapping("/drinks")
    public RecordDtos.RecordView drink(@AuthenticationPrincipal OnePrincipal principal,
                                       @Valid @RequestBody RecordDtos.DrinkRequest request) throws Exception {
        return records.createDrink(principal.userId(), request);
    }

    @PostMapping("/deer")
    public RecordDtos.RecordView deer(@AuthenticationPrincipal OnePrincipal principal,
                                      @Valid @RequestBody RecordDtos.DeerRequest request) {
        return records.createDeer(principal.userId(), request);
    }

    @GetMapping("/today")
    public List<RecordDtos.RecordView> today(@AuthenticationPrincipal OnePrincipal principal) {
        return records.recordsForDay(principal.userId(), LocalDate.now(ZONE));
    }

    @GetMapping
    public List<RecordDtos.RecordView> day(@AuthenticationPrincipal OnePrincipal principal,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return records.recordsForDay(principal.userId(), date);
    }

    @DeleteMapping("/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal OnePrincipal principal, @PathVariable long recordId) {
        records.delete(principal.userId(), recordId);
    }
}
