package com.one.media;

import com.one.common.BusinessException;
import com.one.config.OneProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class MediaService {
    private static final Set<String> SUPPORTED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final MediaAssetRepository assets;
    private final MediaStorage storage;
    private final long maxImageBytes;
    private final ContentSafetyGateway contentSafety;
    private final boolean failClosed;
    private final MeterRegistry meterRegistry;

    public MediaService(MediaAssetRepository assets, MediaStorage storage, ContentSafetyGateway contentSafety,
                        OneProperties properties, MeterRegistry meterRegistry) {
        this.assets = assets;
        this.storage = storage;
        this.maxImageBytes = properties.storage().maxImageBytes();
        this.contentSafety = contentSafety; this.failClosed = properties.contentSafety().failClosed();
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public MediaAsset upload(long userId, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw badImage("请选择一张照片");
        if (file.getSize() > maxImageBytes) throw badImage("照片不能超过 10MB");
        String contentType = file.getContentType();
        if (!SUPPORTED_TYPES.contains(contentType)) throw badImage("仅支持 JPG、PNG 或 WebP 图片");
        byte[] content = file.getBytes();
        MediaStorage.StoredMedia stored = storage.store(contentType, content);
        ContentSafetyGateway.Review review;
        try { review = contentSafety.review(stored.publicUrl()); }
        catch (Exception error) {
            meterRegistry.counter("one.media.safety", "outcome", "unavailable").increment();
            if (failClosed) { storage.delete(stored.storageKey()); throw new BusinessException("CONTENT_SAFETY_UNAVAILABLE", "图片审核暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE); }
            review = new ContentSafetyGateway.Review(true, "NOT_CHECKED");
        }
        if (!review.safe()) { meterRegistry.counter("one.media.safety", "outcome", "rejected").increment(); storage.delete(stored.storageKey()); throw new BusinessException("UNSAFE_IMAGE", "这张图片暂时不能上传，请换一张", HttpStatus.UNPROCESSABLE_ENTITY); }
        meterRegistry.counter("one.media.safety", "outcome", "accepted").increment();
        return assets.save(MediaAsset.ready(userId, stored.storageKey(), stored.publicUrl(), stored.thumbnailUrl(), contentType, content.length, review.label()));
    }

    @Transactional(readOnly = true)
    public MediaAsset owned(long userId, long mediaAssetId) {
        return assets.findById(mediaAssetId).filter(value -> value.getUserId() == userId && value.getStatus() == MediaStatus.READY)
                .orElseThrow(() -> new BusinessException("MEDIA_NOT_FOUND", "照片不存在", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PublicMedia publicMedia(String storageKey) throws IOException {
        MediaAsset asset = assets.findByStorageKeyAndStatus(storageKey, MediaStatus.READY)
                .orElseThrow(() -> new BusinessException("MEDIA_NOT_FOUND", "照片不存在", HttpStatus.NOT_FOUND));
        return new PublicMedia(storage.read(asset.getStorageKey()), asset.getContentType());
    }

    public byte[] content(MediaAsset asset) throws IOException { return storage.read(asset.getStorageKey()); }

    private BusinessException badImage(String message) {
        return new BusinessException("INVALID_IMAGE", message, HttpStatus.BAD_REQUEST);
    }

    public record PublicMedia(byte[] content, String contentType) {}
}
