package com.one.recommendation;

import com.one.security.OnePrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    private final RecommendationService service;
    public RecommendationController(RecommendationService service) { this.service = service; }

    @PostMapping
    public RecommendationDtos.View recommend(@AuthenticationPrincipal OnePrincipal principal,
                                             @Valid @RequestBody RecommendationDtos.Request request) throws Exception {
        return service.recommend(principal.userId(), request);
    }

    @GetMapping("/{sessionId}")
    public RecommendationDtos.View get(@AuthenticationPrincipal OnePrincipal principal,
                                       @PathVariable long sessionId) {
        return service.get(principal.userId(), sessionId);
    }

    @PostMapping("/{sessionId}/candidates/{candidateId}/choose")
    public RecommendationDtos.View choose(@AuthenticationPrincipal OnePrincipal principal,
                                          @PathVariable long sessionId, @PathVariable long candidateId) {
        return service.choose(principal.userId(), sessionId, candidateId);
    }
}
