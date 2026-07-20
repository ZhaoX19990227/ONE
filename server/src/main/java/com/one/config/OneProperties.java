package com.one.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "one")
public record OneProperties(String tokenSecret, Duration tokenTtl, boolean wechatMockEnabled, String demoOpenId,
                            String wechatAppId, String wechatAppSecret,
                            String adminSecret, Qwen qwen, DeepSeek deepseek,
                            Storage storage, ContentSafety contentSafety) {
    public record Qwen(boolean enabled, String apiKey, String workspaceId, String baseUrl,
                       String model, Duration timeout) {}
    public record DeepSeek(boolean enabled, String apiKey, String baseUrl, String model, Duration timeout) {}

    public record Storage(String type, Path root, String publicBaseUrl, long maxImageBytes, Oss oss) {}
    public record Oss(String endpoint, String bucket, String accessKeyId, String accessKeySecret, String keyPrefix) {}
    public record ContentSafety(boolean enabled, boolean failClosed, String endpoint, String service,
                                String accessKeyId, String accessKeySecret) {}
}
