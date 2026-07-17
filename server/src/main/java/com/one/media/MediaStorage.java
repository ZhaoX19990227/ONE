package com.one.media;

import java.io.IOException;

public interface MediaStorage {
    StoredMedia store(String contentType, byte[] content) throws IOException;
    byte[] read(String storageKey) throws IOException;

    record StoredMedia(String storageKey, String publicUrl) {}
}
