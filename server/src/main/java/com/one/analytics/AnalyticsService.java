package com.one.analytics;

import com.one.catalog.CatalogBrand;
import com.one.common.BusinessException;
import com.one.common.Dimension;
import com.one.record.LifeRecord;
import com.one.record.LifeRecordRepository;
import com.one.record.RecordStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private final LifeRecordRepository records;
    public AnalyticsService(LifeRecordRepository records) { this.records = records; }

    @Transactional(readOnly = true)
    public AnalyticsDtos.CalendarMonth month(long userId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);
        List<LifeRecord> values = period(userId, from, to);
        Map<LocalDate, List<LifeRecord>> byDay = groupByDay(values);
        List<AnalyticsDtos.DayCell> days = new ArrayList<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            List<LifeRecord> dayRecords = byDay.getOrDefault(date, List.of());
            days.add(dayCell(date, dayRecords));
        }
        int amount = values.stream().map(LifeRecord::getActualAmountFen).filter(value -> value != null).mapToInt(Integer::intValue).sum();
        return new AnalyticsDtos.CalendarMonth(month, byDay.size(), values.size(), amount, days);
    }

    @Transactional(readOnly = true)
    public AnalyticsDtos.Summary summary(long userId, LocalDate from, LocalDate toInclusive) {
        if (toInclusive.isBefore(from) || ChronoUnit.DAYS.between(from, toInclusive) > 366) {
            throw new BusinessException("INVALID_DATE_RANGE", "统计区间需在 1 到 366 天内", HttpStatus.BAD_REQUEST);
        }
        LocalDate toExclusive = toInclusive.plusDays(1);
        List<LifeRecord> values = period(userId, from, toExclusive);
        Map<LocalDate, List<LifeRecord>> byDay = groupByDay(values);
        int totalAmount = amount(values);

        Map<Dimension, List<LifeRecord>> byDimension = new EnumMap<>(Dimension.class);
        values.forEach(value -> byDimension.computeIfAbsent(value.getRecordType(), ignored -> new ArrayList<>()).add(value));
        List<AnalyticsDtos.DimensionTotal> dimensions = List.of(Dimension.values()).stream()
                .map(dimension -> new AnalyticsDtos.DimensionTotal(dimension,
                        byDimension.getOrDefault(dimension, List.of()).size(),
                        amount(byDimension.getOrDefault(dimension, List.of())))).toList();

        Map<String, BrandAccumulator> brandMap = new LinkedHashMap<>();
        values.stream().filter(value -> value.getRecordType().isShareable())
                .filter(value -> value.getBrandNameSnapshot() != null).forEach(value -> {
                    String key = value.getRecordType() + ":" + value.getBrandNameSnapshot();
                    CatalogBrand brand = value.getBrand();
                    brandMap.computeIfAbsent(key, ignored -> new BrandAccumulator(value.getBrandNameSnapshot(),
                            value.getRecordType(), brand == null ? null : brand.getLogoUrl(),
                            brand == null ? null : brand.getBrandColor())).add(value.getActualAmountFen());
                });
        List<AnalyticsDtos.BrandTotal> topBrands = brandMap.values().stream()
                .sorted(Comparator.comparingInt(BrandAccumulator::count).reversed()
                        .thenComparing(Comparator.comparingInt(BrandAccumulator::amount).reversed()))
                .limit(8).map(BrandAccumulator::view).toList();

        List<AnalyticsDtos.DailyTrend> trend = from.datesUntil(toExclusive)
                .map(date -> new AnalyticsDtos.DailyTrend(date, byDay.getOrDefault(date, List.of()).size(),
                        amount(byDay.getOrDefault(date, List.of())))).toList();
        Integer average = byDay.isEmpty() ? null : totalAmount / byDay.size();
        return new AnalyticsDtos.Summary(from, toInclusive, byDay.size(), values.size(), totalAmount, average,
                dimensions, topBrands, trend, insight(values, totalAmount, byDay.size()));
    }

    @Transactional(readOnly = true)
    public AnalyticsDtos.Weekly weekly(long userId, LocalDate anchor) {
        LocalDate from = anchor.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate to = from.plusDays(6);
        List<LifeRecord> values = period(userId, from, to.plusDays(1));
        Map<LocalDate, List<LifeRecord>> byDay = groupByDay(values);
        List<AnalyticsDtos.DayCell> days = from.datesUntil(to.plusDays(1))
                .map(date -> dayCell(date, byDay.getOrDefault(date, List.of()))).toList();
        List<AnalyticsDtos.DimensionTotal> dimensions = java.util.Arrays.stream(Dimension.values())
                .map(dimension -> {
                    List<LifeRecord> scoped = values.stream().filter(value -> value.getRecordType() == dimension).toList();
                    return new AnalyticsDtos.DimensionTotal(dimension, scoped.size(), amount(scoped));
                }).toList();
        Rhythm rhythm = rhythm(userId, anchor);
        String message = values.isEmpty() ? "这周还很轻，不需要补作业；想留下什么时，ONE 都在。" :
                byDay.size() <= 2 ? "几次真实的记录就很好，生活不需要每天交卷。" :
                        "这一周的选择有了轮廓，但不用为了连续而连续。";
        String easterEgg = rhythm.current() >= 14 ? "你和生活默契地碰了个杯 · 14 DAYS" :
                rhythm.current() >= 7 ? "一小段温柔的连续，被 ONE 看见了 · 7 DAYS" :
                        values.size() >= 8 ? "这周的生活切片，已经能拼成一张小电影。" : null;
        return new AnalyticsDtos.Weekly(from, to, byDay.size(), values.size(), amount(values),
                rhythm.current(), rhythm.longest(), days, dimensions, message, easterEgg);
    }

    private Rhythm rhythm(long userId, LocalDate anchor) {
        LocalDate from = anchor.minusDays(89);
        Map<LocalDate, List<LifeRecord>> byDay = groupByDay(period(userId, from, anchor.plusDays(1)));
        java.util.Set<LocalDate> active = byDay.keySet();
        int longest = 0; int run = 0;
        for (LocalDate date = from; !date.isAfter(anchor); date = date.plusDays(1)) {
            run = active.contains(date) ? run + 1 : 0;
            longest = Math.max(longest, run);
        }
        LocalDate cursor = active.contains(anchor) ? anchor : anchor.minusDays(1);
        int current = 0;
        while (active.contains(cursor)) { current++; cursor = cursor.minusDays(1); }
        return new Rhythm(current, longest);
    }

    private List<LifeRecord> period(long userId, LocalDate from, LocalDate toExclusive) {
        Instant fromInstant = from.atStartOfDay(SHANGHAI).toInstant();
        Instant toInstant = toExclusive.atStartOfDay(SHANGHAI).toInstant();
        return records.findByUserIdAndRecordStatusAndOccurredAtBetweenOrderByOccurredAtAsc(
                userId, RecordStatus.CONFIRMED, fromInstant, toInstant);
    }

    private Map<LocalDate, List<LifeRecord>> groupByDay(List<LifeRecord> values) {
        Map<LocalDate, List<LifeRecord>> result = new LinkedHashMap<>();
        values.forEach(value -> result.computeIfAbsent(value.getOccurredAt().atZone(SHANGHAI).toLocalDate(),
                ignored -> new ArrayList<>()).add(value));
        return result;
    }

    private AnalyticsDtos.DayCell dayCell(LocalDate date, List<LifeRecord> values) {
        int meal = count(values, Dimension.MEAL);
        int tea = count(values, Dimension.MILK_TEA);
        int coffee = count(values, Dimension.COFFEE);
        int habit = count(values, Dimension.PRIVATE_HABIT);
        String cover = values.stream().filter(value -> value.getThumbnailUrl() != null)
                .map(LifeRecord::getThumbnailUrl).findFirst().orElse(null);
        String mood = values.isEmpty() ? null : habit >= 3 ? "今天的森林有点热闹" :
                tea + coffee > 1 ? "今天被饮品温柔接住" : meal > 0 ? "认真吃饭的一天" : "留下一点生活痕迹";
        return new AnalyticsDtos.DayCell(date, values.size(), meal, tea, coffee, habit, amount(values), cover, mood);
    }

    private int count(List<LifeRecord> values, Dimension dimension) {
        return (int) values.stream().filter(value -> value.getRecordType() == dimension).count();
    }

    private int amount(List<LifeRecord> values) {
        return values.stream().map(LifeRecord::getActualAmountFen).filter(value -> value != null).mapToInt(Integer::intValue).sum();
    }

    private String insight(List<LifeRecord> values, int totalAmount, int activeDays) {
        if (values.isEmpty()) return "这个区间还很安静，下一口开始就会有故事。";
        long drinks = values.stream().filter(value -> value.getRecordType() == Dimension.MILK_TEA
                || value.getRecordType() == Dimension.COFFEE).count();
        if (drinks >= 5) return "这一段时间共喝了 " + drinks + " 杯，ONE 正在慢慢记住你的口味。";
        if (totalAmount > 0) return "有记录的 " + activeDays + " 天里，一共花了 ¥" + String.format("%.2f", totalAmount / 100.0) + "。";
        return "你已经留下 " + values.size() + " 条生活切片。";
    }

    private static final class BrandAccumulator {
        private final String name; private final Dimension dimension; private final String logoUrl; private final String color;
        private int count; private int amount;
        private BrandAccumulator(String name, Dimension dimension, String logoUrl, String color) {
            this.name = name; this.dimension = dimension; this.logoUrl = logoUrl; this.color = color;
        }
        void add(Integer value) { count++; if (value != null) amount += value; }
        int count() { return count; }
        int amount() { return amount; }
        AnalyticsDtos.BrandTotal view() { return new AnalyticsDtos.BrandTotal(name, dimension, count, amount, logoUrl, color); }
    }

    private record Rhythm(int current, int longest) {}
}
