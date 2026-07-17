package com.one.recognition;

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
@RequestMapping("/recognitions")
public class RecognitionController {
    private final RecognitionService service;
    public RecognitionController(RecognitionService service) { this.service = service; }

    @PostMapping
    public RecognitionDtos.View recognize(@AuthenticationPrincipal OnePrincipal principal,
                                          @Valid @RequestBody RecognitionDtos.StartRequest request) {
        return service.recognize(principal.userId(), request);
    }

    @GetMapping("/{taskId}")
    public RecognitionDtos.View get(@AuthenticationPrincipal OnePrincipal principal,
                                    @PathVariable long taskId) throws Exception {
        return service.get(principal.userId(), taskId);
    }

    @PostMapping("/{taskId}/confirm")
    public RecognitionDtos.View confirm(@AuthenticationPrincipal OnePrincipal principal,
                                        @PathVariable long taskId,
                                        @Valid @RequestBody RecognitionDtos.ConfirmRequest request) throws Exception {
        return service.confirm(principal.userId(), taskId, request);
    }
}
