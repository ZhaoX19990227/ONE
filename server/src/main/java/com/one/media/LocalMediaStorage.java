package com.one.media;

import com.one.config.OneProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "one.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalMediaStorage implements MediaStorage {
    private final Path root;
    private final String publicBaseUrl;

    public LocalMediaStorage(OneProperties properties) throws IOException {
        this.root = properties.storage().root().toAbsolutePath().normalize();
        this.publicBaseUrl = stripTrailingSlash(properties.storage().publicBaseUrl());
        Files.createDirectories(root);
    }

    @Override
    public StoredMedia store(String contentType, byte[] content) throws IOException {
        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported image content type: " + contentType);
        };
        String key = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = safePath(key);
        Files.write(target, content, StandardOpenOption.CREATE_NEW);
        String url = publicBaseUrl + "/" + key;
        return new StoredMedia(key, url, url);
    }

    @Override
    public byte[] read(String storageKey) throws IOException {
        return Files.readAllBytes(safePath(storageKey));
    }

    @Override
    public void delete(String storageKey) throws IOException { Files.deleteIfExists(safePath(storageKey)); }

    private Path safePath(String key) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        return target;
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
