package com.one.media;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "one.content-safety.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledContentSafetyGateway implements ContentSafetyGateway {
    @Override public Review review(String imageUrl) { return new Review(true, "NOT_CHECKED"); }
}
