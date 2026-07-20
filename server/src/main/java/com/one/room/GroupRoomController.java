package com.one.room;

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
@RequestMapping("/rooms")
public class GroupRoomController {
    private final GroupRoomService rooms;
    public GroupRoomController(GroupRoomService rooms) { this.rooms = rooms; }

    @PostMapping
    public GroupRoomDtos.View create(@AuthenticationPrincipal OnePrincipal principal,
                                     @Valid @RequestBody GroupRoomDtos.CreateRequest request) {
        return rooms.create(principal.userId(), request);
    }
    @GetMapping("/{code}")
    public GroupRoomDtos.View get(@AuthenticationPrincipal OnePrincipal principal, @PathVariable String code) {
        return rooms.get(principal.userId(), code);
    }
    @PostMapping("/{code}/vote")
    public GroupRoomDtos.View vote(@AuthenticationPrincipal OnePrincipal principal, @PathVariable String code,
                                   @Valid @RequestBody GroupRoomDtos.VoteRequest request) {
        return rooms.vote(principal.userId(), code, request.candidateId());
    }
    @PostMapping("/{code}/close")
    public GroupRoomDtos.View close(@AuthenticationPrincipal OnePrincipal principal, @PathVariable String code) {
        return rooms.close(principal.userId(), code);
    }
}
