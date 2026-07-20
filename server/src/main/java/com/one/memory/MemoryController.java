package com.one.memory;

import com.one.common.Dimension;
import com.one.security.OnePrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/memories")
public class MemoryController {
    private final MemoryService memories;

    public MemoryController(MemoryService memories) { this.memories = memories; }

    @GetMapping
    public List<MemoryDtos.View> list(@AuthenticationPrincipal OnePrincipal principal,
                                      @RequestParam(required = false) Dimension dimension) {
        return memories.list(principal.userId(), dimension);
    }

    @DeleteMapping("/{memoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forget(@AuthenticationPrincipal OnePrincipal principal, @PathVariable long memoryId) {
        memories.forget(principal.userId(), memoryId);
    }
}
