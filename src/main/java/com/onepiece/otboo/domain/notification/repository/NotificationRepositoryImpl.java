package com.onepiece.otboo.domain.notification.repository;

import static com.onepiece.otboo.domain.notification.entity.QNotification.notification;

import com.onepiece.otboo.domain.notification.entity.Notification;
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
    public List<Notification> findNotifications(UUID receiverId, Instant createdAtBefore,
        int limit) {
        return queryFactory
            .selectFrom(notification)
            .where(
                notification.receiverId.eq(receiverId),
                createdAtBefore != null ? notification.createdAt.lt(createdAtBefore) : null
            )
            .orderBy(notification.id.desc())
            .limit(limit + 1)
            .fetch();
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
}