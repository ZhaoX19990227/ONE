package com.one.identity;

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

@Entity
@Table(name = "user_account")
public class UserAccount extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "open_id", nullable = false, unique = true, length = 64)
    private String openId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false, length = 40)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "city_code", length = 20)
    private String cityCode;

    @Column(name = "city_name", length = 40)
    private String cityName;

    @Column(length = 160)
    private String bio;

    @Column(name = "interest_tags", columnDefinition = "json")
    private String interestTags;

    @Column(name = "completed_count", nullable = false)
    private int completedCount;

    @Column(name = "cancelled_count", nullable = false)
    private int cancelledCount;

    @Column(name = "no_show_count", nullable = false)
    private int noShowCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Version
    private Long version;

    protected UserAccount() {
    }

    public static UserAccount create(String openId, String nickname) {
        UserAccount user = new UserAccount();
        user.openId = openId;
        user.nickname = nickname;
        user.role = UserRole.MEMBER;
        user.status = UserStatus.ACTIVE;
        user.interestTags = "[]";
        return user;
    }

    public void updateProfile(String nickname, String avatarUrl, String cityCode,
                              String cityName, String bio, String interestTags) {
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.bio = bio;
        this.interestTags = interestTags;
    }

    public void grantRole(UserRole role) {
        this.role = role;
    }

    public Long getId() { return id; }
    public String getOpenId() { return openId; }
    public UserRole getRole() { return role; }
    public String getNickname() { return nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCityCode() { return cityCode; }
    public String getCityName() { return cityName; }
    public String getBio() { return bio; }
    public String getInterestTags() { return interestTags; }
    public int getCompletedCount() { return completedCount; }
    public int getCancelledCount() { return cancelledCount; }
    public int getNoShowCount() { return noShowCount; }
    public UserStatus getStatus() { return status; }
}
