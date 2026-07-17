package com.one.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "one")
public record OneProperties(
        Security security,
        Admin admin,
        Wechat wechat,
        boolean demoDataEnabled
) {
    public record Security(String tokenSecret, Duration tokenTtl) {
    }

    public record Admin(String username, String password) {
    }

    public record Wechat(String appId, String appSecret, boolean mockEnabled) {
    }
}
