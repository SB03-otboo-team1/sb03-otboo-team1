package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowerResponse;
import com.onepiece.otboo.domain.follow.dto.response.FollowingResponse;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import com.onepiece.otboo.global.enums.SortBy;
import com.onepiece.otboo.global.enums.SortDirection;
import java.util.UUID;

public interface FollowService {

    FollowResponse createFollow(FollowRequest request);

    CursorPageResponseDto<FollowerResponse> getFollowers(
        UUID followeeId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        SortBy sortBy,
        SortDirection sortDirection
    );

    CursorPageResponseDto<FollowingResponse> getFollowings(
        UUID followerId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike,
        SortBy sortBy,
        SortDirection sortDirection
    );

    void deleteFollow(FollowRequest request);

    FollowSummaryDto getFollowSummary(UUID userId);
}