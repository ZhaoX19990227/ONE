package com.one.media;

public interface ContentSafetyGateway {
    Review review(String imageUrl) throws Exception;
    record Review(boolean safe, String label) {}
}
