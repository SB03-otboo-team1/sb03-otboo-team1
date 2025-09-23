package com.onepiece.otboo.domain.follow.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class FollowResponse {
    private UUID id;
    private UUID followerId;
    private String nickname;
    private String profileImageUrl;
    private Instant createdAt;

    @QueryProjection
    public FollowResponse(UUID id, UUID followerId, String nickname, String profileImageUrl, Instant createdAt) {
        this.id = id;
        this.followerId = followerId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
    }
}

