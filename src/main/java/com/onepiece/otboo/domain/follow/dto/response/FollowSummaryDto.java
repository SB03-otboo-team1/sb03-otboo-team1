package com.onepiece.otboo.domain.follow.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowSummaryDto(
    UUID followeeId,
    Long followerCount,
    Long followingCount,
    boolean followedByMe,
    UUID followedByMeId,
    boolean followingMe
) {

}