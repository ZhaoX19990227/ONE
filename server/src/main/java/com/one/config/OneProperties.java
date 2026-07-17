package com.one.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "one")
public record OneProperties(String tokenSecret, Duration tokenTtl, boolean wechatMockEnabled, String demoOpenId,
                            String wechatAppId, String wechatAppSecret,
                            Qwen qwen, Storage storage) {
    public record Qwen(boolean enabled, String apiKey, String workspaceId, String baseUrl,
                       String model, Duration timeout) {}

    public record Storage(Path root, String publicBaseUrl, long maxImageBytes) {}
}
