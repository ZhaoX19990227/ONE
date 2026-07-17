package com.one.media;

import com.one.security.OnePrincipal;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) { this.mediaService = mediaService; }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaView upload(@AuthenticationPrincipal OnePrincipal principal,
                            @RequestPart("file") MultipartFile file) throws IOException {
        return MediaView.from(mediaService.upload(principal.userId(), file));
    }

    @GetMapping("/public/{storageKey:.+}")
    public ResponseEntity<byte[]> publicMedia(@PathVariable String storageKey) throws IOException {
        MediaService.PublicMedia media = mediaService.publicMedia(storageKey);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .contentType(MediaType.parseMediaType(media.contentType())).body(media.content());
    }

    public record MediaView(long id, String url, String thumbnailUrl, String contentType, long sizeBytes) {
        static MediaView from(MediaAsset value) {
            return new MediaView(value.getId(), value.getOriginalUrl(), value.getThumbnailUrl(),
                    value.getContentType(), value.getSizeBytes());
        }
    }
}
