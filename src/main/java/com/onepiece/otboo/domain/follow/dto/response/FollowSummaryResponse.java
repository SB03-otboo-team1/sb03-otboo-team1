package com.onepiece.otboo.domain.follow.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowSummaryResponse {
    private UUID userId;
    private long followerCount;
    private long followingCount;

    @JsonProperty("isFollowing")
    private boolean isFollowing;
}