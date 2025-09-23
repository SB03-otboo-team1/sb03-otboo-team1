package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface FollowRepositoryCustom {

    List<FollowResponse> findFollowersWithProfileCursor(
        User followee,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        String sortDirection
    );

    List<FollowingResponse> findFollowingsWithProfileCursor(
        User follower,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        String sortDirection
    );
}