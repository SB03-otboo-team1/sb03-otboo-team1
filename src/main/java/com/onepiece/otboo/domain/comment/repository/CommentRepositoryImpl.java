package com.onepiece.otboo.domain.comment.repository;

import com.onepiece.otboo.domain.comment.entity.Comment;
import com.onepiece.otboo.domain.comment.entity.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findSliceByFeedIdForCursor(UUID feedId, Instant createdAtLt, UUID idLt,
        int limitPlusOne) {
        QComment c = QComment.comment;

        BooleanBuilder where = new BooleanBuilder();
        where.and(c.feed.id.eq(feedId));

        if (createdAtLt != null) {
            if (idLt != null) {
                where.and(
                    c.createdAt.lt(createdAtLt)
                        .or(c.createdAt.eq(createdAtLt).and(c.id.lt(idLt)))
                );
            } else {
                where.and(c.createdAt.lt(createdAtLt));
            }
        }

        return queryFactory.selectFrom(c)
            .where(where)
            .orderBy(c.createdAt.desc(), c.id.desc())
            .limit(limitPlusOne)
            .fetch();
    }
}
