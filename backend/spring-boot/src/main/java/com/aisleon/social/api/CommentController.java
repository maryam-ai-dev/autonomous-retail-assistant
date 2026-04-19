package com.aisleon.social.api;

import com.aisleon.social.CommentTooLongException;
import com.aisleon.social.application.CommentService;
import com.aisleon.social.domain.CommentTarget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/social/comments")
@Tag(name = "Social comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a comment on a post or shared basket")
    @PostMapping
    public ResponseEntity<CommentDto> create(
            Authentication auth, @Valid @RequestBody CreateCommentRequest request) {
        UUID me = UUID.fromString(auth.getPrincipal().toString());
        return ResponseEntity.ok(CommentDto.fromEntity(
                commentService.create(
                        me,
                        request.getTargetType(),
                        request.getTargetId(),
                        request.getBody())));
    }

    @Operation(summary = "List comments for a target (post or shared basket)")
    @GetMapping
    public ResponseEntity<List<CommentDto>> list(
            @RequestParam("targetType") CommentTarget targetType,
            @RequestParam("targetId") UUID targetId) {
        return ResponseEntity.ok(
                commentService.listFor(targetType, targetId).stream()
                        .map(CommentDto::fromEntity)
                        .toList());
    }

    @ExceptionHandler(CommentTooLongException.class)
    public ResponseEntity<Map<String, Object>> handleTooLong(CommentTooLongException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "reason", "COMMENT_TOO_LONG",
                        "actualLength", ex.actualLength(),
                        "maxLength", CommentService.MAX_BODY_LENGTH));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadInput(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("reason", "INVALID_INPUT", "detail", ex.getMessage()));
    }
}
