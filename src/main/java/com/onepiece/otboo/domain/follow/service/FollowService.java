package com.onepiece.otboo.domain.follow.service;

import com.onepiece.otboo.domain.follow.dto.request.FollowRequest;
import com.onepiece.otboo.domain.follow.dto.response.FollowDto;
import com.onepiece.otboo.domain.follow.dto.response.FollowSummaryDto;
import com.onepiece.otboo.global.dto.response.CursorPageResponseDto;
import java.util.UUID;

public interface FollowService {

    FollowDto createFollow(FollowRequest request);

    CursorPageResponseDto<FollowDto> getFollowers(
        UUID followeeId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    );

    CursorPageResponseDto<FollowDto> getFollowings(
        UUID followerId,
        String cursor,
        UUID idAfter,
        int limit,
        String nameLike
    );

    void deleteFollow(FollowRequest request);

    FollowSummaryDto getFollowSummary(UUID userId);
}