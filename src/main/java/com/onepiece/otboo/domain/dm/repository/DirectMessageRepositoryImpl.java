package com.onepiece.otboo.domain.dm.repository;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageResponse.UserInfo;
import com.onepiece.otboo.domain.dm.entity.DirectMessage;
import com.onepiece.otboo.domain.dm.entity.QDirectMessage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.format.DateTimeParseException;
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
    public List<DirectMessageResponse> findDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit,
        String sort
    ) {
        BooleanBuilder whereBuilder = new BooleanBuilder();
        whereBuilder.and(dm.sender.id.eq(userId).or(dm.receiver.id.eq(userId)));

        if (cursor != null && !cursor.isEmpty()) {
            try {
                Instant cursorTime = Instant.parse(cursor);
                BooleanBuilder cursorCondition = new BooleanBuilder();
                cursorCondition.and(dm.createdAt.lt(cursorTime));

                if (idAfter != null) {
                    cursorCondition.or(dm.createdAt.eq(cursorTime).and(dm.id.lt(idAfter)));
                }
                whereBuilder.and(cursorCondition);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                    "Invalid cursor format. Must be ISO-8601 datetime.");
            }
        } else if (idAfter != null) {
            DirectMessage pivot = queryFactory.selectFrom(dm)
                .where(dm.id.eq(idAfter))
                .fetchOne();

            if (pivot != null) {
                BooleanBuilder afterCondition = new BooleanBuilder();
                afterCondition.or(dm.createdAt.gt(pivot.getCreatedAt()));
                afterCondition.or(
                    dm.createdAt.eq(pivot.getCreatedAt())
                        .and(dm.id.gt(pivot.getId()))
                );
                whereBuilder.and(afterCondition);
            }
        }

        OrderSpecifier<?> primaryOrder = dm.createdAt.desc();
        OrderSpecifier<?> secondaryOrder = dm.id.desc();

        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            String sortBy = parts[0];
            String direction = parts.length > 1 ? parts[1].toUpperCase() : "DESC";

            if ("createdAt".equals(sortBy)) {
                primaryOrder = direction.equals("ASC")
                    ? new OrderSpecifier<>(Order.ASC, dm.createdAt)
                    : new OrderSpecifier<>(Order.DESC, dm.createdAt);
                secondaryOrder = direction.equals("ASC")
                    ? new OrderSpecifier<>(Order.ASC, dm.id)
                    : new OrderSpecifier<>(Order.DESC, dm.id);
            } else if ("id".equals(sortBy)) {
                primaryOrder = direction.equals("ASC")
                    ? new OrderSpecifier<>(Order.ASC, dm.id)
                    : new OrderSpecifier<>(Order.DESC, dm.id);
                secondaryOrder = null;
            }
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
            .orderBy(
                secondaryOrder != null
                    ? new OrderSpecifier[]{primaryOrder, secondaryOrder}
                    : new OrderSpecifier[]{primaryOrder}
            )
            .limit(limit)
            .fetch();
    }
}