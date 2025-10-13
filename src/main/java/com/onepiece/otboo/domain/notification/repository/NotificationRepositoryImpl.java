package com.onepiece.otboo.domain.notification.repository;

import com.onepiece.otboo.domain.notification.entity.Notification;
import com.onepiece.otboo.domain.notification.entity.QNotification;
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
        QNotification notification = QNotification.notification;

        var query = queryFactory
            .selectFrom(notification)
            .where(notification.receiverId.eq(receiverId))
            .orderBy(notification.createdAt.desc())
            .limit(limit + 1);

        if (idAfter != null) {
            query.where(notification.id.lt(idAfter));
        }

        return query.fetch();
    }
}