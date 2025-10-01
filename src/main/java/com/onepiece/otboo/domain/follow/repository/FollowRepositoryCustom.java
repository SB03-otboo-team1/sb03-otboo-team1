package com.onepiece.otboo.domain.follow.repository;

import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.domain.user.entity.User;

import com.onepiece.otboo.global.enums.SortDirection;
import java.util.List;
import java.util.UUID;

public interface FollowRepositoryCustom {

    List<FollowerResponse> findFollowersWithProfileCursor(
        User followee,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        SortDirection sortDirection
    );

    List<FollowingResponse> findFollowingsWithProfileCursor(
        User follower,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        String sortBy,
        SortDirection sortDirection
    );
}