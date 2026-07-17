package com.one.activity;

import com.one.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "activity")
public class Activity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ActivityMode mode;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "city_code", length = 20)
    private String cityCode;

    @Column(name = "city_name", length = 40)
    private String cityName;

    @Column(length = 60)
    private String district;

    @Column(length = 200)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "enroll_deadline", nullable = false)
    private Instant enrollDeadline;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "joined_count", nullable = false)
    private int joinedCount;

    @Column(name = "fee_fen", nullable = false)
    private int feeFen;

    @Column(name = "deposit_fen", nullable = false)
    private int depositFen;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    @Column(columnDefinition = "json")
    private String tags;

    @Column(columnDefinition = "json")
    private String attributes;

    @Column(name = "template_version", nullable = false)
    private int templateVersion;

    @Version
    private Long version;

    protected Activity() {
    }

    public static Activity create(ActivityDraft draft) {
        Activity activity = new Activity();
        activity.ownerId = draft.ownerId();
        activity.type = draft.type();
        activity.mode = draft.mode();
        activity.title = draft.title();
        activity.description = draft.description();
        activity.coverUrl = draft.coverUrl();
        activity.cityCode = draft.cityCode();
        activity.cityName = draft.cityName();
        activity.district = draft.district();
        activity.address = draft.address();
        activity.latitude = draft.latitude();
        activity.longitude = draft.longitude();
        activity.startAt = draft.startAt();
        activity.endAt = draft.endAt();
        activity.enrollDeadline = draft.enrollDeadline();
        activity.capacity = draft.capacity();
        activity.feeFen = draft.feeFen();
        activity.depositFen = draft.depositFen();
        activity.approvalRequired = draft.approvalRequired();
        activity.tags = draft.tags();
        activity.attributes = draft.attributes();
        activity.templateVersion = 1;
        activity.status = ActivityStatus.PUBLISHED;
        return activity;
    }

    public boolean hasAvailableSeat() {
        return joinedCount < capacity;
    }

    public void occupySeat() {
        if (!hasAvailableSeat()) {
            throw new IllegalStateException("Activity has no available seat");
        }
        joinedCount++;
        if (joinedCount == capacity) {
            status = ActivityStatus.FULL;
        }
    }

    public void releaseSeat() {
        if (joinedCount <= 0) {
            throw new IllegalStateException("Joined count cannot be negative");
        }
        joinedCount--;
        if (status == ActivityStatus.FULL) {
            status = ActivityStatus.PUBLISHED;
        }
    }

    public boolean isEnrollmentOpen(Instant now) {
        return (status == ActivityStatus.PUBLISHED || status == ActivityStatus.FULL)
                && now.isBefore(enrollDeadline) && now.isBefore(startAt);
    }

    public Long getId() { return id; }
    public Long getOwnerId() { return ownerId; }
    public ActivityType getType() { return type; }
    public ActivityMode getMode() { return mode; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public String getCityCode() { return cityCode; }
    public String getCityName() { return cityName; }
    public String getDistrict() { return district; }
    public String getAddress() { return address; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public Instant getEnrollDeadline() { return enrollDeadline; }
    public int getCapacity() { return capacity; }
    public int getJoinedCount() { return joinedCount; }
    public int getFeeFen() { return feeFen; }
    public int getDepositFen() { return depositFen; }
    public boolean isApprovalRequired() { return approvalRequired; }
    public ActivityStatus getStatus() { return status; }
    public String getTags() { return tags; }
    public String getAttributes() { return attributes; }
    public int getTemplateVersion() { return templateVersion; }

    public record ActivityDraft(
            long ownerId, ActivityType type, ActivityMode mode, String title, String description,
            String coverUrl, String cityCode, String cityName, String district, String address,
            BigDecimal latitude, BigDecimal longitude, Instant startAt, Instant endAt,
            Instant enrollDeadline, int capacity, int feeFen, int depositFen,
            boolean approvalRequired, String tags, String attributes
    ) {
    }
}
