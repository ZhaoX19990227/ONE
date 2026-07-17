package com.one.identity;

import com.one.common.BusinessException;
import com.one.config.OneProperties;
import com.one.security.SessionTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {

    private final WechatIdentityClient wechatIdentityClient;
    private final UserAccountRepository userRepository;
    private final SessionTokenService tokenService;
    private final OneProperties properties;

    public AuthService(WechatIdentityClient wechatIdentityClient, UserAccountRepository userRepository,
                       SessionTokenService tokenService, OneProperties properties) {
        this.wechatIdentityClient = wechatIdentityClient;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    @Transactional
    public LoginResult login(String code) {
        WechatIdentityClient.WechatSession session = wechatIdentityClient.exchangeCode(code);
        UserAccount user = userRepository.findByOpenId(session.openId())
                .orElseGet(() -> userRepository.save(UserAccount.create(session.openId(), "新朋友")));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_UNAVAILABLE", "账号暂时无法使用", HttpStatus.FORBIDDEN);
        }
        return new LoginResult(tokenService.issue(user.getId(), user.getRole()), UserView.from(user));
    }

    @Transactional
    public LoginResult adminLogin(String username, String password) {
        OneProperties.Admin configured = properties.admin();
        if (!constantTimeEquals(configured.username(), username)
                || !constantTimeEquals(configured.password(), password)) {
            throw new BusinessException("ADMIN_LOGIN_FAILED", "管理员账号或密码错误", HttpStatus.UNAUTHORIZED);
        }
        String openId = "admin:" + configured.username();
        UserAccount user = userRepository.findByOpenId(openId)
                .orElseGet(() -> userRepository.save(UserAccount.create(openId, "ONE 运营")));
        if (user.getRole() != UserRole.ADMIN) {
            user.grantRole(UserRole.ADMIN);
        }
        return new LoginResult(tokenService.issue(user.getId(), UserRole.ADMIN), UserView.from(user));
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    public record LoginResult(String token, UserView user) {
    }

    public record UserView(Long id, String nickname, String avatarUrl, String cityName, String role) {
        static UserView from(UserAccount user) {
            return new UserView(user.getId(), user.getNickname(), user.getAvatarUrl(),
                    user.getCityName(), user.getRole().name());
        }
    }
}
