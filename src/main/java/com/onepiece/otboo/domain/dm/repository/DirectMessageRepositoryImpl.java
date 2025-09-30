package com.onepiece.otboo.domain.dm.repository;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse.UserInfo;
import com.onepiece.otboo.domain.dm.entity.QDirectMessage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
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

    private final QDirectMessage dm = QDirectMessage.directMessage;

    @Override
    public List<DirectMessageResponse> findDirectMessages(UUID userId, String cursor, UUID idAfter,
        int limit) {
        BooleanBuilder whereBuilder = new BooleanBuilder();

        whereBuilder.and(dm.sender.id.eq(userId).or(dm.receiver.id.eq(userId)));

        if (cursor != null && !cursor.isEmpty()) {
            Instant cursorTime = Instant.parse(cursor);
            BooleanBuilder cursorCondition = new BooleanBuilder();

            cursorCondition.and(dm.createdAt.lt(cursorTime));

            if (idAfter != null) {
                cursorCondition.or(dm.createdAt.eq(cursorTime).and(dm.id.lt(idAfter)));
            }
            whereBuilder.and(cursorCondition);
        }

        return queryFactory
            .select(Projections.constructor(
                DirectMessageResponse.class,
                dm.id,
                dm.createdAt,
                Projections.constructor(UserInfo.class,
                    dm.sender.id,
                    dm.sender.email
                ),
                Projections.constructor(UserInfo.class,
                    dm.receiver.id,
                    dm.receiver.email
                ),
                dm.content
            ))
            .from(dm)
            .where(whereBuilder)
            .orderBy(dm.createdAt.desc(), dm.id.desc())
            .limit(limit)
            .fetch();
    }
}