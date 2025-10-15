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
        QFollow f = QFollow.follow;

        // 별칭 고정 (재사용 필수)
        QUser followerUser = new QUser("followerUser");
        QUser followeeUser = new QUser("followeeUser");

        QProfile followerProfile = new QProfile("followerProfile");
        QProfile followeeProfile = new QProfile("followeeProfile");

        BooleanBuilder where = new BooleanBuilder()
            .and(f.following.eq(followee));

        if (StringUtils.hasText(nameLike)) {
            where.and(followerProfile.nickname.containsIgnoreCase(nameLike));
        }

        applyCursor(where, f, cursor, idAfter);
        OrderSpecifier<?>[] orders = buildOrders(f);

        return queryFactory
            .selectFrom(f)
            .join(f.follower, followerUser).fetchJoin()
            .join(f.following, followeeUser).fetchJoin()
            // 경로 조인 권장: user.profile
            .leftJoin(followerUser.profile, followerProfile).fetchJoin()
            .leftJoin(followeeUser.profile, followeeProfile).fetchJoin()
            .where(where)
            .orderBy(orders)
            .limit(limit + 1L) // hasNext 판단용
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
        QFollow f = QFollow.follow;

        QUser followerUser = new QUser("followerUser");
        QUser followingUser = new QUser("followingUser");

        QProfile followerProfile = new QProfile("followerProfile");
        QProfile followingProfile = new QProfile("followingProfile");

        BooleanBuilder where = new BooleanBuilder()
            .and(f.follower.eq(follower));

        if (StringUtils.hasText(nameLike)) {
            where.and(followingProfile.nickname.containsIgnoreCase(nameLike));
        }

        applyCursor(where, f, cursor, idAfter);
        OrderSpecifier<?>[] orders = buildOrders(f);

        return queryFactory
            .selectFrom(f)
            .join(f.follower, followerUser).fetchJoin()
            .join(f.following, followingUser).fetchJoin()
            .leftJoin(followerUser.profile, followerProfile).fetchJoin()
            .leftJoin(followingUser.profile, followingProfile).fetchJoin()
            .where(where)
            .orderBy(orders)
            .limit(limit + 1L) // hasNext 판단용
            .fetch();
    }

    /**
     * DESC 커서 조건: createdAt < cursor  OR  (createdAt = cursor AND id < idAfter)
     */
    private void applyCursor(BooleanBuilder where, QFollow f, String cursor, UUID idAfter) {
        if (cursor == null || idAfter == null) {
            return;
        }
        Instant cursorTime = Instant.parse(cursor);
        where.and(
            f.createdAt.lt(cursorTime)
                .or(f.createdAt.eq(cursorTime).and(f.id.lt(idAfter)))
        );
    }

    /**
     * 정렬: createdAt DESC, id DESC
     */
    private OrderSpecifier<?>[] buildOrders(QFollow f) {
        return new OrderSpecifier<?>[]{
            f.createdAt.desc(),
            f.id.desc()
        };
    }
}
