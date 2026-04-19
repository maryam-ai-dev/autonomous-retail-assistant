package com.aisleon.social.api;

import com.aisleon.social.SelfFollowException;
import com.aisleon.social.application.FollowService;
import com.aisleon.social.application.FollowService.FollowStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/social/follows")
@Tag(name = "Social follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @Operation(summary = "Follow a user (idempotent)")
    @PostMapping("/{userId}")
    public ResponseEntity<FollowStatus> follow(
            Authentication auth, @PathVariable("userId") UUID followeeId) {
        UUID me = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(followService.follow(me, followeeId));
    }

    @Operation(summary = "Unfollow a user (idempotent)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<FollowStatus> unfollow(
            Authentication auth, @PathVariable("userId") UUID followeeId) {
        UUID me = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(followService.unfollow(me, followeeId));
    }

    @Operation(summary = "Follow status between the viewer and a target user")
    @GetMapping("/{userId}/status")
    public ResponseEntity<FollowStatus> status(
            Authentication auth, @PathVariable("userId") UUID followeeId) {
        UUID me = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(followService.status(me, followeeId));
    }

    @ExceptionHandler(SelfFollowException.class)
    public ResponseEntity<Map<String, String>> handleSelf(SelfFollowException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("reason", "SELF_FOLLOW_NOT_ALLOWED"));
    }
}
