package com.one.integration.ai;

import com.one.activity.ActivityMode;
import com.one.activity.ActivityType;

import java.util.List;
import java.util.Map;

public interface ActivityDraftAssistant {

    DraftSuggestion suggest(String naturalLanguage);

    record DraftSuggestion(
            ActivityType type,
            ActivityMode mode,
            String suggestedTitle,
            String description,
            Integer capacity,
            Integer depositFen,
            List<String> tags,
            Map<String, Object> attributes,
            List<String> fieldsRequiringConfirmation,
            String provider
    ) {
    }
}
