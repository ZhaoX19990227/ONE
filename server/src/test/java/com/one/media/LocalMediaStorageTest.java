package com.one.media;

import com.one.config.OneProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalMediaStorageTest {
    @TempDir Path tempDirectory;

    @Test
    void shouldStoreAndReadImageWithOpaqueKey() throws Exception {
        LocalMediaStorage storage = new LocalMediaStorage(properties());
        byte[] content = new byte[]{1, 2, 3};

        MediaStorage.StoredMedia stored = storage.store("image/png", content);

        assertThat(stored.storageKey()).endsWith(".png").doesNotContain("/");
        assertThat(stored.publicUrl()).startsWith("https://one.test/api/media/public/");
        assertThat(storage.read(stored.storageKey())).containsExactly(content);
    }

    @Test
    void shouldRejectUnsupportedContentType() throws Exception {
        LocalMediaStorage storage = new LocalMediaStorage(properties());
        assertThatThrownBy(() -> storage.store("image/svg+xml", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private OneProperties properties() {
        return new OneProperties("test-secret-with-at-least-32-characters", Duration.ofHours(1), true,
                "demo", "", "", new OneProperties.Qwen(false, "", "", "", "qwen3.6-flash", Duration.ofSeconds(30)),
                new OneProperties.Storage(tempDirectory, "https://one.test/api/media/public", 10_485_760));
    }
}
