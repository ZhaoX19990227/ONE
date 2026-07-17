package com.one.identity;

import com.one.config.OneProperties;
import com.one.security.SessionTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final SessionTokenService tokens;
    private final OneProperties properties;
    private final WechatCode2SessionClient wechatClient;

    public AuthService(UserAccountRepository users, SessionTokenService tokens, OneProperties properties,
                       WechatCode2SessionClient wechatClient) {
        this.users = users;
        this.tokens = tokens;
        this.properties = properties;
        this.wechatClient = wechatClient;
    }

    @Transactional
    public LoginResult login(String code) {
        String openId = properties.wechatMockEnabled()
                ? properties.demoOpenId() + ":" + Integer.toHexString(code.hashCode())
                : wechatClient.resolveOpenId(code);
        UserAccount user = users.findByOpenId(openId)
                .orElseGet(() -> users.save(UserAccount.create(openId, "ONE群友")));
        return new LoginResult(tokens.issue(user.getId()), user.getId(), user.getNickname(), user.getAvatarUrl());
    }

    public record LoginResult(String token, long userId, String nickname, String avatarUrl) {}
}
