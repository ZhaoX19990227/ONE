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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "open_id", nullable = false, unique = true, length = 64)
    private String openId;
    @Column(nullable = false, length = 40)
    private String nickname;
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    @Column(name = "height_cm")
    private Integer heightCm;
    @Column(name = "weight_gram")
    private Integer weightGram;
    @Enumerated(EnumType.STRING) @Column(name = "gay_role", length = 16)
    private GayRole gayRole;
    @Column(name = "role_visibility", nullable = false, length = 16)
    private String roleVisibility = "PRIVATE";
    @Column(name = "meal_preferences", columnDefinition = "json")
    private String mealPreferences;
    @Column(name = "drink_preferences", columnDefinition = "json")
    private String drinkPreferences;
    @Column(name = "dietary_restrictions", columnDefinition = "json")
    private String dietaryRestrictions;
    @Column(name = "monthly_budget_fen")
    private Integer monthlyBudgetFen;
    @Column(name = "ai_enabled", nullable = false)
    private boolean aiEnabled = true;
    @Column(name = "private_habit_enabled", nullable = false)
    private boolean privateHabitEnabled = true;
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";
    @Version private long version;

    protected UserAccount() {}

    public static UserAccount create(String openId, String nickname) {
        UserAccount user = new UserAccount();
        user.openId = openId;
        user.nickname = nickname;
        user.mealPreferences = "{}";
        user.drinkPreferences = "{}";
        user.dietaryRestrictions = "[]";
        return user;
    }

    public void updateProfile(String nickname, String avatarUrl, Integer heightCm, Integer weightGram,
                              GayRole gayRole, Integer monthlyBudgetFen, boolean aiEnabled,
                              boolean privateHabitEnabled, String mealPreferences,
                              String drinkPreferences, String dietaryRestrictions) {
        this.nickname = nickname.strip();
        this.avatarUrl = avatarUrl;
        this.heightCm = heightCm;
        this.weightGram = weightGram;
        this.gayRole = gayRole;
        this.monthlyBudgetFen = monthlyBudgetFen;
        this.aiEnabled = aiEnabled;
        this.privateHabitEnabled = privateHabitEnabled;
        this.mealPreferences = mealPreferences;
        this.drinkPreferences = drinkPreferences;
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public Long getId() { return id; }
    public String getOpenId() { return openId; }
    public String getNickname() { return nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public Integer getHeightCm() { return heightCm; }
    public Integer getWeightGram() { return weightGram; }
    public GayRole getGayRole() { return gayRole; }
    public String getRoleVisibility() { return roleVisibility; }
    public String getMealPreferences() { return mealPreferences; }
    public String getDrinkPreferences() { return drinkPreferences; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }
    public Integer getMonthlyBudgetFen() { return monthlyBudgetFen; }
    public boolean isAiEnabled() { return aiEnabled; }
    public boolean isPrivateHabitEnabled() { return privateHabitEnabled; }
}
