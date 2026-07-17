package com.one.identity;

import com.one.common.BusinessException;
import com.one.security.OnePrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/me")
public class ProfileController {
    private final UserAccountRepository users;
    private final ObjectMapper objectMapper;

    public ProfileController(UserAccountRepository users, ObjectMapper objectMapper) {
        this.users = users;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ProfileView get(@AuthenticationPrincipal OnePrincipal principal) { return ProfileView.from(find(principal.userId())); }

    @PutMapping
    @Transactional
    public ProfileView update(@AuthenticationPrincipal OnePrincipal principal,
                              @Valid @RequestBody UpdateProfileRequest request) throws Exception {
        UserAccount user = find(principal.userId());
        user.updateProfile(request.nickname(), request.avatarUrl(), request.heightCm(), request.weightGram(),
                request.gayRole(), request.monthlyBudgetFen(), request.aiEnabled(), request.privateHabitEnabled(),
                objectMapper.writeValueAsString(request.mealPreferences()),
                objectMapper.writeValueAsString(request.drinkPreferences()),
                objectMapper.writeValueAsString(request.dietaryRestrictions()));
        return ProfileView.from(user);
    }

    private UserAccount find(long id) {
        return users.findById(id).orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在", HttpStatus.NOT_FOUND));
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 40) String nickname,
            @Size(max = 500) String avatarUrl,
            @Min(120) @Max(230) Integer heightCm,
            @Min(25_000) @Max(250_000) Integer weightGram,
            GayRole gayRole,
            @Min(0) @Max(10_000_000) Integer monthlyBudgetFen,
            boolean aiEnabled,
            boolean privateHabitEnabled,
            Map<String, Object> mealPreferences,
            Map<String, Object> drinkPreferences,
            List<String> dietaryRestrictions) {
        public UpdateProfileRequest {
            mealPreferences = mealPreferences == null ? Map.of() : Map.copyOf(mealPreferences);
            drinkPreferences = drinkPreferences == null ? Map.of() : Map.copyOf(drinkPreferences);
            dietaryRestrictions = dietaryRestrictions == null ? List.of() : List.copyOf(dietaryRestrictions);
        }
    }

    public record ProfileView(long id, String nickname, String avatarUrl, Integer heightCm, Integer weightGram,
                              GayRole gayRole, Integer monthlyBudgetFen, boolean aiEnabled,
                              boolean privateHabitEnabled, String mealPreferences,
                              String drinkPreferences, String dietaryRestrictions) {
        static ProfileView from(UserAccount user) {
            return new ProfileView(user.getId(), user.getNickname(), user.getAvatarUrl(), user.getHeightCm(),
                    user.getWeightGram(), user.getGayRole(), user.getMonthlyBudgetFen(), user.isAiEnabled(),
                    user.isPrivateHabitEnabled(), user.getMealPreferences(), user.getDrinkPreferences(),
                    user.getDietaryRestrictions());
        }
    }
}
