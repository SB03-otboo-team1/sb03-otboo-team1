package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.entity.QFollow;
import com.onepiece.otboo.domain.profile.entity.QProfile;
import com.onepiece.otboo.domain.user.entity.QUser;
import com.onepiece.otboo.domain.user.entity.User;
import com.onepiece.otboo.global.enums.SortDirection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final SortDirection SORT_DIRECTION = SortDirection.DESCENDING;

    @Override
    public List<Follow> findFollowersWithProfileCursor(
        User followee,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    ) {
        QFollow follow = QFollow.follow;
        QUser followerUser = new QUser("followerUser");
        QUser followeeUser = new QUser("followeeUser");
        QProfile followerProfile = new QProfile("followerProfile"); // follower의 프로필
        QProfile followeeProfile = new QProfile("followeeProfile"); // followee의 프로필

        BooleanBuilder where = new BooleanBuilder()
            .and(follow.following.eq(followee));

        // nameLike는 follower의 profile.nickname 기준
        if (StringUtils.hasText(nameLike)) {
            where.and(followerProfile.nickname.containsIgnoreCase(nameLike));
        }

        if (cursor != null && idAfter != null) {
            Instant cursorTime = Instant.parse(cursor);
            if (SORT_DIRECTION.equals(SortDirection.ASCENDING)) {
                where.and(
                    follow.createdAt.gt(cursorTime)
                        .or(follow.createdAt.eq(cursorTime).and(follow.id.gt(idAfter)))
                );
            } else {
                where.and(
                    follow.createdAt.lt(cursorTime)
                        .or(follow.createdAt.eq(cursorTime).and(follow.id.lt(idAfter)))
                );
            }
        }

        OrderSpecifier<?> orderByCreatedAt = SORT_DIRECTION.equals(SortDirection.ASCENDING)
            ? follow.createdAt.asc() : follow.createdAt.desc();
        OrderSpecifier<?> orderById = SORT_DIRECTION.equals(SortDirection.ASCENDING)
            ? follow.id.asc() : follow.id.desc();

        return queryFactory
            .selectFrom(follow)
            .join(follow.follower, followerUser).fetchJoin()
            .join(follow.following, followeeUser).fetchJoin()
            // 프로필 조인 (N+1 방지)
            .leftJoin(followerProfile).on(followerProfile.user.eq(followerUser)).fetchJoin()
            .leftJoin(followeeProfile).on(followeeProfile.user.eq(followeeUser)).fetchJoin()
            .where(where)
            .orderBy(orderByCreatedAt, orderById)
            .limit(limit + 1L)
            .fetch();
    }

    @Override
    public List<Follow> findFollowingsWithProfileCursor(
        User follower,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    ) {
        QFollow follow = QFollow.follow;
        QUser followerUser = new QUser("followerUser");
        QUser followingUser = new QUser("followingUser");
        QProfile followerProfile = new QProfile("followerProfile");
        QProfile followingProfile = new QProfile("followingProfile"); // following의 프로필

        BooleanBuilder where = new BooleanBuilder()
            .and(follow.follower.eq(follower));

        // nameLike는 following의 profile.nickname 기준
        if (StringUtils.hasText(nameLike)) {
            where.and(followingProfile.nickname.containsIgnoreCase(nameLike));
        }

        if (cursor != null && idAfter != null) {
            Instant cursorTime = Instant.parse(cursor);
            if (SORT_DIRECTION.equals(SortDirection.ASCENDING)) {
                where.and(
                    follow.createdAt.gt(cursorTime)
                        .or(follow.createdAt.eq(cursorTime).and(follow.id.gt(idAfter)))
                );
            } else {
                where.and(
                    follow.createdAt.lt(cursorTime)
                        .or(follow.createdAt.eq(cursorTime).and(follow.id.lt(idAfter)))
                );
            }
        }

        OrderSpecifier<?> orderByCreatedAt = SORT_DIRECTION.equals(SortDirection.ASCENDING)
            ? follow.createdAt.asc() : follow.createdAt.desc();
        OrderSpecifier<?> orderById = SORT_DIRECTION.equals(SortDirection.ASCENDING)
            ? follow.id.asc() : follow.id.desc();

        return queryFactory
            .selectFrom(follow)
            .join(follow.follower, followerUser).fetchJoin()
            .join(follow.following, followingUser).fetchJoin()
            // 프로필 조인 (N+1 방지)
            .leftJoin(followerProfile).on(followerProfile.user.eq(followerUser)).fetchJoin()
            .leftJoin(followingProfile).on(followingProfile.user.eq(followingUser)).fetchJoin()
            .where(where)
            .orderBy(orderByCreatedAt, orderById)
            .limit(limit + 1)
            .fetch();
    }
}
