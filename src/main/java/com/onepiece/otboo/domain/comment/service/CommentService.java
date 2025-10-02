package com.onepiece.otboo.domain.comment.service;

import com.onepiece.otboo.domain.comment.dto.request.CommentCreateRequest;
import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.comment.entity.Comment;
import com.onepiece.otboo.domain.comment.mapper.CommentMapper;
import com.onepiece.otboo.domain.comment.repository.CommentRepository;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final EntityManager em;
    private final CommentRepository repository;
    private final CommentMapper mapper;

    public CommentDto create(UUID feedIdFromPath, CommentCreateRequest req) {
        if (req.feedId() != null && !req.feedId().equals(feedIdFromPath)) {
            throw new IllegalArgumentException("요청 본문의 feedId가 path의 feedId와 일치하지 않습니다.");
        }

        Feed feed = em.find(Feed.class, feedIdFromPath);
        if (feed == null) {
            throw new EntityNotFoundException("존재하지 않는 피드입니다.");
        }

        User author = em.find(User.class, req.authorId());
        if (author == null) {
            throw new EntityNotFoundException("존재하지 않는 사용자(authorId)입니다.");
        }

        Comment comment = Comment.builder()
            .feed(feed)
            .author(author)
            .content(req.content().trim())
            .build();

        repository.save(comment);
        return mapper.toDto(comment);
    }
}
