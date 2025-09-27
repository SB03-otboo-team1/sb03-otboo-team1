package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.follow.dto.response.QFollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.QFollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.QFollowingResponse;
import com.onepiece.otboo.domain.follow.entity.QFollow;
import com.onepiece.otboo.domain.profile.entity.QProfile;
import com.onepiece.otboo.domain.user.entity.QUser;
import com.onepiece.otboo.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FollowingResponse> findFollowingsWithProfileCursor(
        User follower,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        String sortDirection
    ) {
        QFollow follow = QFollow.follow;
        QUser user = QUser.user;
        QProfile profile = QProfile.profile;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(follow.follower.eq(follower));

        if (nameLike != null && !nameLike.isBlank()) {
            builder.and(profile.nickname.containsIgnoreCase(nameLike));
        }

        if (cursor != null && idAfter != null) {
            Instant cursorTime = Instant.parse(cursor);
            if ("ASC".equalsIgnoreCase(sortDirection)) {
                builder.and(follow.createdAt.gt(cursorTime)
                    .or(follow.createdAt.eq(cursorTime).and(follow.id.gt(idAfter))));
            } else {
                builder.and(follow.createdAt.lt(cursorTime)
                    .or(follow.createdAt.eq(cursorTime).and(follow.id.lt(idAfter))));
            }
        }

        OrderSpecifier<?> orderSpecifier =
            "ASC".equalsIgnoreCase(sortDirection)
                ? follow.createdAt.asc()
                : follow.createdAt.desc();

        return queryFactory
            .select(new QFollowingResponse(
                follow.id,
                user.id,
                profile.nickname,
                profile.profileImageUrl,
                follow.createdAt
            ))
            .from(follow)
            .join(follow.following, user)
            .leftJoin(profile).on(profile.user.eq(user))
            .where(builder)
            .orderBy(orderSpecifier, follow.id.asc())
            .limit(limit + 1)
            .fetch();
    }

    @Override
    public List<FollowerResponse> findFollowersWithProfileCursor(
        User followee,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        String sortDirection
    ) {
        QFollow follow = QFollow.follow;
        QUser user = QUser.user;
        QProfile profile = QProfile.profile;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(follow.following.eq(followee));

        if (nameLike != null && !nameLike.isBlank()) {
            builder.and(profile.nickname.containsIgnoreCase(nameLike));
        }

        if (cursor != null && idAfter != null) {
            Instant cursorTime = Instant.parse(cursor);
            if ("ASC".equalsIgnoreCase(sortDirection)) {
                builder.and(follow.createdAt.gt(cursorTime)
                    .or(follow.createdAt.eq(cursorTime).and(follow.id.gt(idAfter))));
            } else {
                builder.and(follow.createdAt.lt(cursorTime)
                    .or(follow.createdAt.eq(cursorTime).and(follow.id.lt(idAfter))));
            }
        }

        OrderSpecifier<?> orderSpecifier =
            "ASC".equalsIgnoreCase(sortDirection)
                ? follow.createdAt.asc()
                : follow.createdAt.desc();

        return queryFactory
            .select(new QFollowerResponse(
                follow.id,
                user.id,
                profile.nickname,
                profile.profileImageUrl,
                follow.createdAt
            ))
            .from(follow)
            .join(follow.follower, user)
            .leftJoin(profile).on(profile.user.eq(user))
            .where(builder)
            .orderBy(orderSpecifier, follow.id.asc())
            .limit(limit + 1)
            .fetch();
    }
}