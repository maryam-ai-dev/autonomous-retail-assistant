package com.aisleon.social.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aisleon.social.CommentTooLongException;
import com.aisleon.social.domain.CommentTarget;
import com.aisleon.social.infrastructure.CommentJpaEntity;
import com.aisleon.social.infrastructure.CommentRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommentServiceTest {

    private final CommentRepository repo = mock(CommentRepository.class);
    private final CommentService service = new CommentService(repo);

    @Test
    void createsOnPostTarget() {
        when(repo.save(any())).thenAnswer(inv -> {
            CommentJpaEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(UUID.randomUUID());
            return e;
        });

        CommentJpaEntity comment = service.create(
                UUID.randomUUID(), CommentTarget.POST, UUID.randomUUID(), "great basket");
        assertThat(comment.getTargetType()).isEqualTo(CommentTarget.POST);
        assertThat(comment.getBody()).isEqualTo("great basket");
    }

    @Test
    void createsOnSharedBasketTarget() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommentJpaEntity comment = service.create(
                UUID.randomUUID(), CommentTarget.SHARED_BASKET, UUID.randomUUID(), "nice!");
        assertThat(comment.getTargetType()).isEqualTo(CommentTarget.SHARED_BASKET);
    }

    @Test
    void rejectsCommentOverFiveHundredChars() {
        String tooLong = "x".repeat(CommentService.MAX_BODY_LENGTH + 1);
        assertThatThrownBy(() -> service.create(
                        UUID.randomUUID(), CommentTarget.POST, UUID.randomUUID(), tooLong))
                .isInstanceOf(CommentTooLongException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsEmptyComment() {
        assertThatThrownBy(() -> service.create(
                        UUID.randomUUID(), CommentTarget.POST, UUID.randomUUID(), "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listReturnsFromRepository() {
        UUID targetId = UUID.randomUUID();
        CommentJpaEntity a = CommentJpaEntity.builder()
                .id(UUID.randomUUID())
                .targetType(CommentTarget.POST)
                .targetId(targetId)
                .authorUserId(UUID.randomUUID())
                .body("hi")
                .reported(false)
                .build();
        when(repo.findByTargetTypeAndTargetIdAndReportedFalseOrderByCreatedAtDesc(
                        CommentTarget.POST, targetId))
                .thenReturn(List.of(a));

        List<CommentJpaEntity> out = service.listFor(CommentTarget.POST, targetId);
        assertThat(out).containsExactly(a);
    }
}
