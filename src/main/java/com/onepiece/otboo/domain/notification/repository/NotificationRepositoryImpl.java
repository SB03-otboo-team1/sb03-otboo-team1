package com.onepiece.otboo.domain.notification.repository;

import static com.onepiece.otboo.domain.notification.entity.QNotification.notification;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findNotifications(Instant cursor, UUID idAfter, int limit) {
        return queryFactory
            .selectFrom(notification)
            .where(buildCursorCondition(cursor, idAfter))
            .orderBy(notification.createdAt.desc(), notification.id.desc())
            .limit(limit + 1)
            .fetch();
    }

    private BooleanExpression buildCursorCondition(Instant cursor, UUID idAfter) {
        if (cursor == null) {
            return null;
        }

        BooleanExpression beforeCreatedAt = notification.createdAt.lt(cursor);
        BooleanExpression sameTimeSmallerId = notification.createdAt.eq(cursor);

        if (idAfter != null) {
            sameTimeSmallerId = sameTimeSmallerId.and(notification.id.lt(idAfter));
        }

        return beforeCreatedAt.or(sameTimeSmallerId);
    }

    @Override
    public long countByReceiverId(UUID receiverId) {
        Long count = queryFactory
            .select(notification.count())
            .from(notification)
            .where(notification.receiverId.eq(receiverId))
            .fetchOne();

        return count != null ? count : 0L;
    }

    @Override
    public long countAll() {
        Long count = queryFactory
            .select(notification.count())
            .from(notification)
            .fetchOne();

        return count != null ? count : 0L;
    }
}