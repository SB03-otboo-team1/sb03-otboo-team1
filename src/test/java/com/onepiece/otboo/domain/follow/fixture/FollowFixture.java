package com.onepiece.otboo.domain.follow.fixture;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.entity.Follow;
import com.onepiece.otboo.domain.user.entity.User;

import java.util.UUID;

public class FollowFixture {

    public static FollowRequest createFollowRequest(UUID followerId, UUID followingId) {
        return new FollowRequest(followerId, followingId);
    }

    public static Follow createFollow(User follower, User following) {
        return Follow.builder()
            .follower(follower)
            .following(following)
            .build();
    }
}