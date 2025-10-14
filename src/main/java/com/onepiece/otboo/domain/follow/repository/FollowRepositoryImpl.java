package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.follow.entity.QFollow;
import com.onepiece.otboo.domain.profile.entity.QProfile;
import com.onepiece.otboo.domain.user.entity.QUser;
import com.onepiece.otboo.domain.user.entity.User;
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

        // nameLike → follower의 프로필 닉네임 기준
        if (StringUtils.hasText(nameLike)) {
            where.and(followerProfile.nickname.containsIgnoreCase(nameLike));
        }

        // 커서 조건
        applyCursor(where, follow, cursor, idAfter);

        // 정렬 지정 (항상 DESC)
        OrderSpecifier<?>[] orders = buildOrders(follow);

        return baseQuery(limit, follow, followerUser, followeeUser, followerProfile,
            followeeProfile, where, orders);
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
        QProfile followingProfile = new QProfile("followingProfile");

        BooleanBuilder where = new BooleanBuilder()
            .and(follow.follower.eq(follower));

        // nameLike → following의 프로필 닉네임 기준
        if (StringUtils.hasText(nameLike)) {
            where.and(followingProfile.nickname.containsIgnoreCase(nameLike));
        }

        applyCursor(where, follow, cursor, idAfter);
        OrderSpecifier<?>[] orders = buildOrders(follow);

        return baseQuery(limit, follow, followerUser, followingUser, followerProfile,
            followingProfile, where, orders);
    }

    /**
     * 커서 조건: DESC 기준 (createdAt < cursor) OR (createdAt = cursor AND id < idAfter)
     */
    private void applyCursor(BooleanBuilder where, QFollow follow, String cursor, UUID idAfter) {
        if (cursor == null || idAfter == null) {
            return;
        }

        Instant cursorTime = Instant.parse(cursor);
        where.and(
            follow.createdAt.lt(cursorTime)
                .or(follow.createdAt.eq(cursorTime).and(follow.id.lt(idAfter)))
        );
    }

    /**
     * 정렬 조건: 항상 DESC
     */
    private OrderSpecifier<?>[] buildOrders(QFollow follow) {
        return new OrderSpecifier<?>[]{
            follow.createdAt.desc(),
            follow.id.desc()
        };
    }

    /**
     * Follow 엔티티 fetchJoin 공통 쿼리 limit+1 로 hasNext 판별 가능
     */
    private List<Follow> baseQuery(
        int limit,
        QFollow follow,
        QUser leftUser,     // followerUser
        QUser rightUser,    // followingUser or followeeUser
        QProfile leftProfile,
        QProfile rightProfile,
        BooleanBuilder where,
        OrderSpecifier<?>[] orders
    ) {
        return queryFactory
            .selectFrom(follow)
            .join(follow.follower, new QUser(leftUser.getMetadata())).fetchJoin()
            .join(follow.following, new QUser(rightUser.getMetadata())).fetchJoin()
            .leftJoin(leftProfile).on(leftProfile.user.eq(leftUser)).fetchJoin()
            .leftJoin(rightProfile).on(rightProfile.user.eq(rightUser)).fetchJoin()
            .where(where)
            .orderBy(orders)
            .limit(limit + 1L)
            .fetch();
    }
}
