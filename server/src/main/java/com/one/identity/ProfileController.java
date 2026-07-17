package com.one.identity;

import com.one.common.BusinessException;
import com.one.security.OnePrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/me")
public class ProfileController {

    private final UserAccountRepository userRepository;
    private final ObjectMapper objectMapper;

    public ProfileController(UserAccountRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ProfileView get(@AuthenticationPrincipal OnePrincipal principal) {
        return ProfileView.from(findUser(principal.userId()));
    }

    @PatchMapping
    @Transactional
    public ProfileView update(@AuthenticationPrincipal OnePrincipal principal,
                              @Valid @RequestBody UpdateProfileRequest request) throws JacksonException {
        UserAccount user = findUser(principal.userId());
        user.updateProfile(request.nickname(), request.avatarUrl(), request.cityCode(), request.cityName(),
                request.bio(), objectMapper.writeValueAsString(request.interestTags()));
        return ProfileView.from(user);
    }

    private UserAccount findUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new BusinessException("USER_NOT_FOUND", "用户不存在", HttpStatus.NOT_FOUND));
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 40) String nickname,
            @Size(max = 500) String avatarUrl,
            @Size(max = 20) String cityCode,
            @Size(max = 40) String cityName,
            @Size(max = 160) String bio,
            @Size(max = 20) List<@Size(max = 20) String> interestTags
    ) {
        public UpdateProfileRequest {
            interestTags = interestTags == null ? List.of() : List.copyOf(interestTags);
        }
    }

    public record ProfileView(Long id, String nickname, String avatarUrl, String cityCode, String cityName,
                              String bio, String interestTags, int completedCount, int cancelledCount,
                              int noShowCount) {
        static ProfileView from(UserAccount user) {
            return new ProfileView(user.getId(), user.getNickname(), user.getAvatarUrl(), user.getCityCode(),
                    user.getCityName(), user.getBio(), user.getInterestTags(), user.getCompletedCount(),
                    user.getCancelledCount(), user.getNoShowCount());
        }
    }
}
