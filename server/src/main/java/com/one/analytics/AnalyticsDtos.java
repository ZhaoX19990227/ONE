package com.one.analytics;

import com.one.common.Dimension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public final class AnalyticsDtos {
    private AnalyticsDtos() {}

    public record DayCell(LocalDate date, int totalCount, int mealCount, int milkTeaCount,
                          int coffeeCount, int privateHabitCount, int amountFen,
                          String coverUrl, String moodText) {}

    public record CalendarMonth(YearMonth month, int activeDays, int totalCount,
                                int totalAmountFen, List<DayCell> days) {}

    public record DimensionTotal(Dimension dimension, int count, int amountFen) {}
    public record BrandTotal(String brandName, Dimension dimension, int count, int amountFen,
                             String logoUrl, String color) {}
    public record DailyTrend(LocalDate date, int count, int amountFen) {}
    public record Summary(LocalDate from, LocalDate to, int activeDays, int totalCount,
                          int totalAmountFen, Integer averageAmountPerActiveDayFen,
                          List<DimensionTotal> dimensions, List<BrandTotal> topBrands,
                          List<DailyTrend> trend, String insight) {}
}
