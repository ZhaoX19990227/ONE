package com.one.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "media_asset")
public class MediaAsset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @Column(name = "storage_key", nullable = false, length = 300) private String storageKey;
    @Column(name = "original_url", nullable = false, length = 500) private String originalUrl;
    @Column(name = "thumbnail_url", length = 500) private String thumbnailUrl;
    @Column(name = "content_type", nullable = false, length = 80) private String contentType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private MediaStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected MediaAsset() {}

    public static MediaAsset ready(long userId, String storageKey, String publicUrl,
                                   String contentType, long sizeBytes) {
        MediaAsset asset = new MediaAsset();
        asset.userId = userId;
        asset.storageKey = storageKey;
        asset.originalUrl = publicUrl;
        asset.thumbnailUrl = publicUrl;
        asset.contentType = contentType;
        asset.sizeBytes = sizeBytes;
        asset.status = MediaStatus.READY;
        return asset;
    }

    @PrePersist
    void beforeCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public String getStorageKey() { return storageKey; }
    public String getOriginalUrl() { return originalUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public MediaStatus getStatus() { return status; }
}
