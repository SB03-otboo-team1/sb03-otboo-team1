package com.onepiece.otboo.domain.dm.repository;

import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto;
import com.onepiece.otboo.domain.dm.dto.response.DirectMessageDto.UserDto;
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
    public List<DirectMessageDto> findDirectMessages(
        UUID userId,
        String cursor,
        UUID idAfter,
        int limit,
        String sort
    ) {
        BooleanBuilder whereBuilder = new BooleanBuilder();
        whereBuilder.and(dm.sender.id.eq(userId).or(dm.receiver.id.eq(userId)));

        boolean isAscending = false;

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
                afterCondition.or(dm.createdAt.lt(pivot.getCreatedAt()));
                afterCondition.or(
                    dm.createdAt.eq(pivot.getCreatedAt()).and(dm.id.lt(pivot.getId())));
                whereBuilder.and(afterCondition);
            }
        }

        OrderSpecifier<?> primaryOrder = new OrderSpecifier<>(Order.DESC, dm.createdAt);
        OrderSpecifier<?> secondaryOrder = new OrderSpecifier<>(Order.DESC, dm.id);

        return queryFactory
            .select(Projections.constructor(
                DirectMessageDto.class,
                dm.id,
                dm.createdAt,
                Projections.constructor(UserDto.class,
                    dm.sender.id,
                    dm.sender.email
                ),
                Projections.constructor(UserDto.class,
                    dm.receiver.id,
                    dm.receiver.email
                ),
                dm.content
            ))
            .from(dm)
            .where(whereBuilder)
            .orderBy(primaryOrder, secondaryOrder)
            .limit(limit)
            .fetch();
    }
}