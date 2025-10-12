package com.onepiece.otboo.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.comment.entity.Comment;
import com.onepiece.otboo.domain.comment.mapper.CommentMapper;
import com.onepiece.otboo.domain.comment.repository.CommentRepository;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository repository;
    @Mock
    CommentMapper mapper;
    @Mock
    EntityManager em;

    @InjectMocks
    CommentService service;

    UUID feedId;
    UUID authorId;

    @BeforeEach
    void setUp() {
        feedId = UUID.randomUUID();
        authorId = UUID.randomUUID();
    }

    @Test
    void 댓글_등록_성공() {
        // given
        CommentCreateRequest req = new CommentCreateRequest(null, authorId, "댓글");
        Feed feed = mock(Feed.class);
        User author = mock(User.class);

        when(em.find(Feed.class, feedId)).thenReturn(feed);
        when(em.find(User.class, authorId)).thenReturn(author);

        CommentDto expected = new CommentDto(
            UUID.randomUUID(),
            Instant.now(),
            feedId,
            new com.onepiece.otboo.domain.feed.dto.response.AuthorDto(authorId, "", null),
            "댓글"
        );
        when(mapper.toDto(any(Comment.class))).thenReturn(expected);

        // when
        CommentDto actual = service.create(feedId, req);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.feedId()).isEqualTo(feedId);
        assertThat(actual.author().userId()).isEqualTo(authorId);
        assertThat(actual.content()).isEqualTo("댓글");

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(repository, times(1)).save(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getFeed()).isEqualTo(feed);
        assertThat(saved.getAuthor()).isEqualTo(author);
        assertThat(saved.getContent()).isEqualTo("댓글");

        verify(mapper, times(1)).toDto(saved);
    }


    @Test
    void 존재하지_않는_피드() {
        // given
        CommentCreateRequest req = new CommentCreateRequest(null, authorId, "내용");
        when(em.find(Feed.class, feedId)).thenReturn(null);

        // expect
        assertThatThrownBy(() -> service.create(feedId, req))
            .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).save(any());
        verify(mapper, never()).toDto(any());
    }

    @Test
    void 존재하지_않는_작성자() {
        // given
        CommentCreateRequest req = new CommentCreateRequest(null, authorId, "내용");
        when(em.find(Feed.class, feedId)).thenReturn(mock(Feed.class));
        when(em.find(User.class, authorId)).thenReturn(null);

        // expect
        assertThatThrownBy(() -> service.create(feedId, req))
            .isInstanceOf(EntityNotFoundException.class);
        verify(repository, never()).save(any());
        verify(mapper, never()).toDto(any());
    }
}
