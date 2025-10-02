package com.onepiece.otboo.domain.user.repository;

import static com.onepiece.otboo.domain.user.entity.QUser.user;

import com.onepiece.otboo.domain.profile.entity.QProfile;
import com.onepiece.otboo.domain.user.dto.request.UserGetRequest;
import com.onepiece.otboo.domain.user.dto.response.UserDto;
import com.onepiece.otboo.domain.user.enums.Role;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QProfile profile = QProfile.profile;

    @Override
    public List<UserDto> findUsers(UserGetRequest userGetRequest) {
        String cursor = userGetRequest.cursor();
        UUID nextAfter = userGetRequest.idAfter();
        Role roleEqual = userGetRequest.roleEqual();
        Boolean locked = userGetRequest.locked();
        String emailLike = userGetRequest.emailLike();
        int limit = userGetRequest.limit();

        BooleanBuilder where = getWhere(emailLike, roleEqual, locked);

        if (cursor != null && nextAfter != null) {
            boolean asc = userGetRequest.isAscending();
            switch (userGetRequest.sortByEnum()) {
                case CREATED_AT -> {
                    Instant cAt = UserGetRequest.parseCreatedAtStrict(cursor);
                    if (asc) {
                        where.and(user.createdAt.gt(cAt)
                            .or(user.createdAt.eq(cAt).and(user.id.gt(nextAfter))));
                    } else {
                        where.and(user.createdAt.lt(cAt)
                            .or(user.createdAt.eq(cAt).and(user.id.lt(nextAfter))));
                    }
                }
                case EMAIL -> {
                    if (asc) {
                        where.and(user.email.gt(cursor)
                            .or(user.email.eq(cursor).and(user.id.gt(nextAfter))));
                    } else {
                        where.and(user.email.lt(cursor)
                            .or(user.email.eq(cursor).and(user.id.lt(nextAfter))));
                    }
                }
            }
        }

        OrderSpecifier<?> primary = switch (userGetRequest.sortByEnum()) {
            case CREATED_AT ->
                (userGetRequest.isAscending() ? user.createdAt.asc() : user.createdAt.desc());
            case EMAIL -> (userGetRequest.isAscending() ? user.email.asc() : user.email.desc());
        };
        OrderSpecifier<?> tieBreaker = (userGetRequest.isAscending() ? user.id.asc()
            : user.id.desc());

        return queryFactory.select(Projections.constructor(UserDto.class,
                user.id, user.createdAt, user.email, profile.nickname, user.role,
                user.socialAccount.provider,
                user.locked))
            .from(user)
            .leftJoin(profile).on(profile.user.id.eq(user.id))
            .where(where)
            .orderBy(primary, tieBreaker)
            .limit(limit + 1)
            .fetch();
    }

    @Override
    public long countUsers(String emailLike, Role roleEqual, Boolean locked) {
        BooleanBuilder where = getWhere(emailLike, roleEqual, locked);

        Long count = queryFactory.select(user.id.countDistinct())
            .from(user)
            .where(where)
            .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanBuilder getWhere(String emailLike, Role roleEqual, Boolean locked) {
        BooleanBuilder where = new BooleanBuilder();

        if (emailLike != null && !emailLike.isBlank()) {
            where.and(user.email.containsIgnoreCase(emailLike));
        }
        if (roleEqual != null) {
            where.and(user.role.eq(roleEqual));
        }
        if (locked != null) {
            where.and(user.locked.eq(locked));
        }
        return where;
    }
}
