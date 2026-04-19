package com.aisleon.social.api;

import com.aisleon.social.PostNotFoundException;
import com.aisleon.social.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/social/posts")
@Tag(name = "Social posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Create a social post")
    @PostMapping
    public ResponseEntity<PostDto> create(
            Authentication auth, @Valid @RequestBody CreatePostRequest request) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(postService.create(userId, request));
    }

    @Operation(
            summary = "Read one page of the social feed",
            description = "Cursor pagination by created_at timestamp, descending.")
    @GetMapping("/feed")
    public ResponseEntity<FeedPageDto> feed(
            Authentication auth,
            @RequestParam(value = "cursor", required = false) String cursor) {
        UUID viewerId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(postService.feed(viewerId, cursor));
    }

    @Operation(summary = "Fetch a single post by id")
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> get(Authentication auth, @PathVariable UUID id) {
        UUID viewerId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(postService.get(id, viewerId));
    }

    @Operation(summary = "Toggle a reaction on a post")
    @PostMapping("/{id}/react")
    public ResponseEntity<PostDto> react(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody ReactionRequest request) {
        UUID userId = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(postService.react(id, userId, request.getReactionType()));
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PostNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("reason", "POST_NOT_FOUND"));
    }
}
