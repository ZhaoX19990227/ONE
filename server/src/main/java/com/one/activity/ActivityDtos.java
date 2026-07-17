package com.one.activity;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ActivityDtos {

    private ActivityDtos() {
    }

    public record CreateActivityRequest(
            @NotNull ActivityType type,
            @NotNull ActivityMode mode,
            @NotBlank @Size(max = 80) String title,
            @NotBlank @Size(max = 1000) String description,
            @Size(max = 500) String coverUrl,
            @Size(max = 20) String cityCode,
            @Size(max = 40) String cityName,
            @Size(max = 60) String district,
            @Size(max = 200) String address,
            BigDecimal latitude,
            BigDecimal longitude,
            @NotNull @Future Instant startAt,
            @NotNull @Future Instant endAt,
            @NotNull @Future Instant enrollDeadline,
            @Min(2) @Max(100) int capacity,
            @Min(0) @Max(1_000_000) int feeFen,
            @Min(0) @Max(100_000) int depositFen,
            boolean approvalRequired,
            @Size(max = 12) List<@Size(max = 20) String> tags,
            Map<String, Object> attributes
    ) {
        public CreateActivityRequest {
            tags = tags == null ? List.of() : List.copyOf(tags);
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        }
    }

    public record ActivitySummary(
            long id, ActivityType type, ActivityMode mode, String title, String coverUrl,
            String cityName, String district, Instant startAt, int capacity, int joinedCount,
            int feeFen, int depositFen, ActivityStatus status, String tags, String recommendationReason
    ) {
        static ActivitySummary from(Activity activity) {
            String reason = activity.getJoinedCount() + 1 >= activity.getCapacity()
                    ? "差一人成局" : "时间与你最近的偏好相符";
            return new ActivitySummary(activity.getId(), activity.getType(), activity.getMode(),
                    activity.getTitle(), activity.getCoverUrl(), activity.getCityName(), activity.getDistrict(),
                    activity.getStartAt(), activity.getCapacity(), activity.getJoinedCount(), activity.getFeeFen(),
                    activity.getDepositFen(), activity.getStatus(), activity.getTags(), reason);
        }
    }

    public record ActivityDetail(
            long id, long ownerId, ActivityType type, ActivityMode mode, String title,
            String description, String coverUrl, String cityName, String district, String address,
            BigDecimal latitude, BigDecimal longitude, Instant startAt, Instant endAt,
            Instant enrollDeadline, int capacity, int joinedCount, int feeFen, int depositFen,
            boolean approvalRequired, ActivityStatus status, String tags, String attributes
    ) {
        static ActivityDetail from(Activity activity, boolean revealExactLocation) {
            return new ActivityDetail(activity.getId(), activity.getOwnerId(), activity.getType(), activity.getMode(),
                    activity.getTitle(), activity.getDescription(), activity.getCoverUrl(), activity.getCityName(),
                    activity.getDistrict(), revealExactLocation ? activity.getAddress() : null,
                    revealExactLocation ? activity.getLatitude() : null,
                    revealExactLocation ? activity.getLongitude() : null,
                    activity.getStartAt(), activity.getEndAt(), activity.getEnrollDeadline(), activity.getCapacity(),
                    activity.getJoinedCount(), activity.getFeeFen(), activity.getDepositFen(),
                    activity.isApprovalRequired(), activity.getStatus(), activity.getTags(), activity.getAttributes());
        }
    }
}
