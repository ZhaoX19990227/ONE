package com.one.media;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.one.config.OneProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "one.storage.type", havingValue = "oss")
public class OssMediaStorage implements MediaStorage {
    private final OSS client;
    private final String bucket;
    private final String keyPrefix;
    private final String publicBaseUrl;

    public OssMediaStorage(OneProperties properties) {
        OneProperties.Storage storage = properties.storage(); OneProperties.Oss oss = storage.oss();
        require(oss.endpoint(), "OSS_ENDPOINT"); require(oss.bucket(), "OSS_BUCKET");
        require(oss.accessKeyId(), "ALIBABA_CLOUD_ACCESS_KEY_ID"); require(oss.accessKeySecret(), "ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        require(storage.publicBaseUrl(), "ONE_MEDIA_BASE_URL");
        this.client = new OSSClientBuilder().build(oss.endpoint(), oss.accessKeyId(), oss.accessKeySecret());
        this.bucket = oss.bucket(); this.keyPrefix = trimSlash(oss.keyPrefix());
        this.publicBaseUrl = trimTrailingSlash(storage.publicBaseUrl());
    }

    @Override
    public StoredMedia store(String contentType, byte[] content) {
        String extension = extension(contentType); LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String key = keyPrefix + "/" + today.getYear() + "/" + String.format("%02d", today.getMonthValue()) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;
        ObjectMetadata metadata = new ObjectMetadata(); metadata.setContentType(contentType); metadata.setContentLength(content.length);
        client.putObject(bucket, key, new ByteArrayInputStream(content), metadata);
        String url = publicBaseUrl + "/" + key;
        return new StoredMedia(key, url, url + "?x-oss-process=image/resize,w_320/format,webp");
    }

    @Override
    public byte[] read(String storageKey) throws IOException {
        try (var object = client.getObject(bucket, storageKey); var input = object.getObjectContent()) { return input.readAllBytes(); }
    }

    @Override
    public void delete(String storageKey) { client.deleteObject(bucket, storageKey); }

    @PreDestroy public void shutdown() { client.shutdown(); }
    private static String extension(String type) { return switch (type) { case "image/jpeg" -> ".jpg"; case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> throw new IllegalArgumentException("Unsupported type"); }; }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalStateException(name + " is required for OSS storage"); }
    private static String trimSlash(String value) { if (value == null || value.isBlank()) return "one/media"; return value.replaceAll("^/+|/+$", ""); }
    private static String trimTrailingSlash(String value) { return value.endsWith("/") ? value.substring(0, value.length() - 1) : value; }
}
