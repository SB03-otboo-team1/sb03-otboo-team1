package com.onepiece.otboo.domain.comment.service;

import com.onepiece.otboo.domain.comment.dto.response.CommentDto;
import com.onepiece.otboo.domain.comment.entity.Comment;
import com.onepiece.otboo.domain.comment.mapper.CommentMapper;
import com.onepiece.otboo.domain.comment.repository.CommentRepository;
import com.onepiece.otboo.domain.feed.entity.Feed;
import com.onepiece.otboo.domain.feed.enums.FeedSortBy;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortDirection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EntityManager em;

    /**
     * 피드별 댓글 목록 조회 (커서 페이징).
     *
     * 정렬: createdAt DESC, id DESC (tie-breaker)
     * 커서 포맷: Base64(URL-safe) 로 인코딩된 "epochMilli:id"
     *  - epochMilli: 마지막 행의 createdAt.toEpochMilli() (null이면 0)
     *  - id: 마지막 행의 UUID
     *
     * @param feedId  대상 피드 ID
     * @param cursor  다음 페이지 커서 (직전 응답의 nextCursor)
     * @param idAfter 경계 ID (cursor가 없을 때 기준점으로 사용)
     * @param limit   페이지 크기 (권장 1~100)
     * @return CursorPageResponseDto<CommentDto>
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public CursorPageResponseDto<CommentDto> listByFeed(UUID feedId, String cursor, UUID idAfter, int limit) {
        // 피드 존재 확인 (전역 예외 정책이 있다면 거기에 맞게 치환 가능)
        Feed feed = em.find(Feed.class, feedId);
        if (feed == null) {
            throw new EntityNotFoundException("Feed not found: " + feedId);
        }

        int pageSize = normalizeLimit(limit);

        Cursor decoded = decodeCursor(cursor);

        if ((decoded.createdAtLt == null || decoded.idLt == null) && idAfter != null) {
            Comment pivot = commentRepository.findById(idAfter)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + idAfter));
            Instant pivotCreatedAt = Objects.requireNonNullElse(pivot.getCreatedAt(), Instant.EPOCH);
            decoded = new Cursor(pivotCreatedAt, pivot.getId());
        }

        List<Comment> rows = commentRepository.findSliceByFeedIdForCursor(
            feedId,
            decoded.createdAtLt,
            decoded.idLt,
            pageSize + 1
        );

        boolean hasNext = rows.size() > pageSize;
        if (hasNext) {
            rows = rows.subList(0, pageSize);
        }

        List<CommentDto> data = rows.stream()
            .map(commentMapper::toDto)
            .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;

        if (hasNext && !rows.isEmpty()) {
            Comment last = rows.get(rows.size() - 1);
            Instant lastCreatedAt = Objects.requireNonNullElse(last.getCreatedAt(), Instant.EPOCH);
            nextCursor = encodeCursor(lastCreatedAt, last.getId());
            nextIdAfter = last.getId();
        }

        long totalCount = commentRepository.countByFeed_Id(feedId);

        // 메타 값 표준화: sortBy="createdAt", sortDirection="DESCENDING"
        String sortBy = FeedSortBy.createdAt.name();
        String sortDirection = SortDirection.DESCENDING.name();

        return new CursorPageResponseDto<>(
            data,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            sortBy,
            sortDirection
        );
    }

    private static final String CURSOR_SEP = ":";

    private record Cursor(Instant createdAtLt, UUID idLt) {}

    private Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new Cursor(null, null);
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String token = new String(decoded, StandardCharsets.UTF_8);
            int idx = token.indexOf(CURSOR_SEP);
            if (idx <= 0 || idx >= token.length() - 1) {
                return new Cursor(null, null);
            }
            String epochStr = token.substring(0, idx);
            String idStr = token.substring(idx + 1);

            long epochMilli = Long.parseLong(epochStr);
            Instant createdAtLt = Instant.ofEpochMilli(epochMilli);
            UUID idLt = UUID.fromString(idStr);
            return new Cursor(createdAtLt, idLt);
        } catch (Exception e) {
            return new Cursor(null, null);
        }
    }

    private String encodeCursor(Instant createdAt, UUID id) {
        String raw = createdAt.toEpochMilli() + CURSOR_SEP + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) return 1;
        if (limit > 100) return 100;
        return limit;
    }
}
