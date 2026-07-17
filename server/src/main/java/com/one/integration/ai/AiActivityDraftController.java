package com.one.integration.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiActivityDraftController {

    private final ActivityDraftAssistant activityDraftAssistant;

    public AiActivityDraftController(ActivityDraftAssistant activityDraftAssistant) {
        this.activityDraftAssistant = activityDraftAssistant;
    }

    @PostMapping("/activity-draft")
    public ActivityDraftAssistant.DraftSuggestion suggest(@Valid @RequestBody DraftRequest request) {
        return activityDraftAssistant.suggest(request.text());
    }

    public record DraftRequest(@NotBlank @Size(max = 500) String text) {
    }
}
