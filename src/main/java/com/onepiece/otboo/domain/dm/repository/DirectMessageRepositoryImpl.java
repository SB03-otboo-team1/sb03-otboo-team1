package com.onepiece.otboo.domain.dm.repository;

import static com.onepiece.otboo.domain.dm.entity.QDirectMessage.directMessage;

import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DirectMessage> findDirectMessages(
        UUID myId,
        UUID otherId,
        String cursor,
        UUID idAfter,
        int limit
    ) {
        // 대화 상대 조건 (양방향)
        BooleanBuilder base = new BooleanBuilder()
            .and(
                directMessage.sender.id.eq(myId).and(directMessage.receiver.id.eq(otherId))
                    .or(directMessage.sender.id.eq(otherId).and(directMessage.receiver.id.eq(myId)))
            );

        // DESC 고정: cursor가 있으면 (createdAt < cursor) OR (createdAt = cursor AND id < idAfter)
        if (cursor != null && !cursor.isBlank()) {
            Instant c = Instant.parse(cursor);
            BooleanBuilder before = new BooleanBuilder()
                .and(directMessage.createdAt.lt(c)
                    .or(
                        directMessage.createdAt.eq(c)
                            .and(idAfter != null
                                ? directMessage.id.lt(idAfter)
                                : directMessage.id.isNotNull()
                            )
                    )
                );
            base.and(before);
        }

        return queryFactory.selectFrom(directMessage)
            .where(base)
            .orderBy(
                directMessage.createdAt.desc(),
                directMessage.id.desc()
            )
            .limit(limit + 1)
            .fetch();
    }

    @Override
    public long countConversation(UUID myId, UUID otherId) {
        Long count = queryFactory.select(directMessage.count())
            .from(directMessage)
            .where(
                directMessage.sender.id.eq(myId).and(directMessage.receiver.id.eq(otherId))
                    .or(directMessage.sender.id.eq(otherId).and(directMessage.receiver.id.eq(myId)))
            )
            .fetchOne();

        return count != null ? count : 0L;
    }
}
