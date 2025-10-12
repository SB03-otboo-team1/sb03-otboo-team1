package com.onepiece.otboo.domain.notification.repository;

import static com.onepiece.otboo.domain.notification.entity.QNotification.notification;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findNotifications(UUID receiverId, UUID idAfter, int limit) {
        return queryFactory
            .selectFrom(notification)
            .where(
                notification.receiverId.eq(receiverId),
                idAfter != null ? notification.id.lt(idAfter) : null
            )
            .orderBy(notification.createdAt.desc())
            .limit(limit + 1)
            .fetch();
    }

    @Override
    public long countByReceiverId(UUID receiverId) {
        return queryFactory
            .select(notification.count())
            .from(notification)
            .where(notification.receiverId.eq(receiverId))
            .fetchFirst();
    }
}